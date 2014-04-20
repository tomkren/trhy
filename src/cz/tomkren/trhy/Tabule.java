package cz.tomkren.trhy;

import java.util.*;

/**
 *
 * @author Tomáš Křen
 */


public class Tabule {
    
    private Commodity commodity;

    private PriorityQueue<Row> supply;
    private PriorityQueue<Row> demand;
            
    public static class Row {
        
        int    transID;   // transaction ID
        String agentID;   // agent ID, abych věděl koho informovat
        String firmID;    // firm ID
        double price;     // price
        double num;       // number of items
        int    tik;       // tik zadání
        
        public Row (int tid, String aid, String fid, double p, double n, int t) {
            transID = tid; agentID = aid; firmID  = fid; price = p; num = n; tik = t;
        } 
        
        public int    getTID () { return transID; }
        public String getAID () { return agentID; }
        public String getFID () { return firmID;  }
        public int    getStartTik () { return tik;}
        
        public double getValue () { return num*price; }
        public double getPrice () { return price; }
        public double getNum   () { return num; }
                
        public void decreaseNum (double n) { num -= n;}
        
        @Override
        public String toString() {
            return "$"+price+" ... "+num+" ks ... "+firmID+" ("+agentID+") [tid "+transID+" tik "+ tik +"]";
        }
    }

    
    public Tabule(Commodity commodity) {
        this.commodity = commodity;
        int initialCapacity = 11; //11 je prej default
        supply = new PriorityQueue<Row>(initialCapacity, new MinRowComparator()); 
        demand = new PriorityQueue<Row>(initialCapacity, new MaxRowComparator());
    }
    
    public List<Transaction.Result> add (Transaction.Request tr, int transID , int currentTik) {
        // TODO rozdělané
      
        if (tr instanceof Transaction.QBuy) {
            Transaction.QBuy qbuy = (Transaction.QBuy)tr;
            double moneyToSpend = qbuy.getMoney();
            return quickBuy(moneyToSpend, tr, transID, currentTik);
        }

        if (tr instanceof Transaction.SBuy) {
            return slowBuy((Transaction.SBuy)tr, transID, currentTik);
        }

        if (tr instanceof Transaction.SSell) {
            return slowSell((Transaction.SSell)tr, transID, currentTik);
        }

        // ... ostatní případy

        
        return null;
    }

    private List<Transaction.Result> slowBuy (Transaction.SBuy tre, int transID, int currentTik) {

        // TODO potřeba ošetřit případ kdy je některá cena nižší než zde uvedená (a rovnou nakoupit!)
        // TODO udělat asi pomocí zobecneni quickBuy_rec přidanim parametru priceLimit kterej bude pro QBUY nastaven na 0

        double price = tre.getPrice();
        double num   = tre.getMoney()/price;

        demand.add(new Row(transID, tre.getAID(), tre.getFID(),   price  , num, currentTik));

        return new LinkedList<Transaction.Result>();
    }

    private List<Transaction.Result> slowSell (Transaction.SSell tre, int transID, int currentTik) {

        // TODO potřeba ošetřit případ kdy je některá cena nižší než zde uvedená (a rovnou prodat!)
        // TODO udělat asi pomocí zobecneni quickSell_rec přidanim ... analogicky s úpravou ve slow buy

        double price = tre.getPrice();
        double num   = tre.getNum();

        supply.add(new Row(transID, tre.getAID(), tre.getFID(),   price  , num, currentTik));

        return new LinkedList<Transaction.Result>();
    }

    private List<Transaction.Result> quickBuy (double moneyToSpend, Transaction.Request tr, int transID, int currentTik) {
        return quickBuy_rec(moneyToSpend, tr, transID, currentTik, new LinkedList<Transaction.Result>());
    }

    private final static Transaction.Result.ResultType BUY      = Transaction.Result.ResultType.BUY;
    private final static Transaction.Result.ResultType SELL     = Transaction.Result.ResultType.SELL;
    private final static Transaction.Result.ResultType NO_Q_BUY = Transaction.Result.ResultType.NO_Q_BUY;


    /* TODO Poznámky:
    *  Zdá se mi potřeba držet si standard čistýho kódu, proto ze všeho nejdřív vyřešim tyhle nedodělaný funkce a pak je
    *  budu refaktorovat na co nejjednoduší formu bez code repetitions.
    *
    *  Jakej je rozdíl mezi slowBuy a quickBuy?
    *  Fundamentální rozdíl co je činí těžce vzajemně zobecnitelnýma je, že quick se hned vrátí, zatímco když dám
    *  slowBuy s cenou 0 (pokud by to šlo), tak tam zůstane (pokud je nabídka prázdná, případně uspokojí jen z části)
    *  to by ale šlo popsat nějakym booleanem stayThere (== isQuick) kterej určuje jestli se po uspokojení toho co jde
    *  zbytek vrátí jako neuspokojen, nebo se zapíše do tabule.
    *
    *  Proto navrhuju udělat zobecněnej buy_rec navíc s parametry priceLimit a stayThere a pak napsat analogicky
    *  sell_rec, tyto dva pak následně zobecnit do jedný věci.
    *
    *  Případně i odstranit rekurzi (nahrazení while cyklem) (mělo by bejt rychlejší, možná že bude i přehlednější).
    *
    *
    * */

    private List<Transaction.Result> buy (Transaction.Buy buyTransReq, int transID, int currentTik) {

        boolean isQuick  = buyTransReq instanceof Transaction.Quick;
        double  buyPrice = 0;
        double  moneyToSpend = buyTransReq.getMoney();
        List<Transaction.Result> acc = new LinkedList<Transaction.Result>();

        if (!isQuick) {
            Transaction.Slow slowTransReq = (Transaction.Slow) buyTransReq ;
            buyPrice = slowTransReq.getPrice() ;
        }

        return buy_rec(isQuick, buyPrice, moneyToSpend, (Transaction.Request)buyTransReq, transID, currentTik, acc);
    }

    private List<Transaction.Result> buy_rec (  boolean                   isQuick         ,
                                                double                    buyPrice        ,
                                                double                    moneyToSpend    ,
                                                Transaction.Request       tre             ,
                                                int                       transID         ,
                                                int                       currentTik      ,
                                                List<Transaction.Result>  acc             ) {

        if (moneyToSpend <= 0) { return acc; } // better safe than sorry

        // ošetří zda neni třeba odeslat zprávu o neutracených penězích z Q_BUY kvůli prázdné nabídce.
        if (isQuick && supply.isEmpty()) {
            acc.add(new Transaction.Result(NO_Q_BUY, transID, tre.getAID(), tre.getFID(), commodity, 0,moneyToSpend, 0, currentTik, currentTik));
            return acc;
        }

        // vyndáme důležité položky z "nejvýhodnějšího" řádku
        Row    row      = supply.peek();
        double rowValue = row.getValue();
        double rowPrice = row.getPrice();
        double rowNum   = row.getNum();

        // pro pomalou nákupní transakcí s moc nízkou nákupní cenou (oproti nejlepší nabídce) přidáme nový poptávkový řádek a končíme
        if (!isQuick && rowPrice > buyPrice) {
            // pro jistotu ještě zde kontrola kladnosti nákupní ceny
            if (buyPrice <= 0) {return acc;}
            // přidáme řádek
            demand.add(new Row(transID, tre.getAID(), tre.getFID(), buyPrice  , moneyToSpend/buyPrice , currentTik));
            // a žádné další nákupy se zatím nekonají
            return acc;
        }

        // isOverflow znamená, že nákupní request nebude plně uspokojen tímto řádkem
        boolean isOverflow = (moneyToSpend > rowValue);
        // kolik kusu tedy koupím
        double numToBuy = isOverflow ? rowNum : moneyToSpend/rowPrice;
        // .. a kolik mě to bude stát
        double moneyForBuy = numToBuy*rowPrice;

        // budeme odebírat řádek, případně ho jen upravovat
        if (isOverflow) {
            supply.poll();
        } else {
            row.decreaseNum(numToBuy);
            if (row.getNum() <= 0) { // jsou to doubly, tak by to mohlo jit zaokrouhlením pod
                supply.poll(); //řádek už je prázdný, vyhodíme
            }
        }

        // Musíme vrátit zprávu (Transaction.Result) jak pro nakupujícího, tak pro toho komu ten řádek patřil
        // (a) zpráva pro nakupujícího
        // (b) zpráva pro prodávajícího
        acc.add(new Transaction.Result(BUY,  transID,      tre.getAID(), tre.getFID(), commodity, numToBuy, moneyForBuy , rowPrice, currentTik,        currentTik ));
        acc.add(new Transaction.Result(SELL, row.getTID(), row.getAID(), row.getFID(), commodity, numToBuy, moneyForBuy , rowPrice, row.getStartTik(), currentTik ));

        if (isOverflow) { return buy_rec(isQuick, buyPrice, moneyToSpend - rowValue, tre, transID, currentTik, acc); }
        else            { return acc; }
    }

    private List<Transaction.Result> sell_rec ( boolean                   isQuick         ,
                                                double                    sellPrice       ,
                                                double                    sellNum         ,
                                                Transaction.Request       tre             ,
                                                int                       transID         ,
                                                int                       currentTik      ,
                                                List<Transaction.Result>  acc             ) {



        return null;
    }

    private List<Transaction.Result> quickBuy_rec(double moneyToSpend, Transaction.Request tre, int transID, int currentTik, List<Transaction.Result> acc) {


        //TODO : možná rozšířit o parametr priceLimit (a přejmenovat na buy_rec), jak je uvedeno v todo k slowBuy

        //TODO : čečit esli nahodou neni prázdná tabulka!!!!

        if (supply.isEmpty()) {
            // TODO musíme poslat spešl rezult kterej vrátí peníze
            acc.add(new Transaction.Result(NO_Q_BUY, transID, tre.getAID(), tre.getFID(), commodity, 0,moneyToSpend, 0, currentTik, currentTik));
            return acc;
        }
        
        if (moneyToSpend <= 0) { return acc; } // radši pojistka

        Row    row      = supply.peek();
        double rowValue = row.getValue();
        double rowPrice = row.getPrice();
        double rowNum   = row.getNum();

        boolean preteklo = (moneyToSpend > rowValue);

        double numToBuy    = preteklo ? rowNum : moneyToSpend/rowPrice;
        double moneyForBuy = numToBuy*rowPrice;

        if (preteklo) {
            supply.poll();
        } else {
            row.decreaseNum(numToBuy);
            if (row.getNum() <= 0) { // jsou to doubly, tak by to mohlo jit zaokrouhlenim pod
                supply.poll(); //řádek už je prázdný, vyhodímež
            }
        }

        acc.add(new Transaction.Result(BUY,  transID,      tre.getAID(), tre.getFID(), commodity, numToBuy, moneyForBuy , rowPrice, currentTik,        currentTik ));
        acc.add(new Transaction.Result(SELL, row.getTID(), row.getAID(), row.getFID(), commodity, numToBuy, moneyForBuy , rowPrice, row.getStartTik(), currentTik ));

        if (preteklo) { return quickBuy_rec(moneyToSpend - rowValue, tre, transID, currentTik, acc); }
        else          { return acc; }
    }
    
 
    
    public Row bestDemand () { return demand.peek(); }
    public Row bestSupply () { return supply.peek(); }
    
    public double buyMarketPrice  () { return bestSupply().price; }
    public double sellMarketPrice () { return bestSupply().price; }
    
    public static void main (String[] args) {
        Log.it("Tabule main, hello!");
        
        Commodity pie = new Commodity.Basic("Koláč");

        Tabule t = new Tabule(pie);
               
        t.supply.add(new Row(3, "žid",   "Koloniál",  44, 3   ,10));
        t.supply.add(new Row(1, "pekař", "Pekař&Syn", 42, 10  ,1));
        t.supply.add(new Row(2, "pekař2", "Pekař&Syn2", 43, 110 ,5));

        t.demand.add(new Row(30, "otrokář", "UKsro",    41  ,   3, 100));
        t.demand.add(new Row(10, "žid",     "Koloniál", 40  ,  10, 20));
        t.demand.add(new Row(20, "otrokář", "UKsro",    41.5, 110, 3));
        
        
        Log.it().it(t);
        
        Log.it("Best supply price: $" + t.supply.peek().price);
        Log.it("Best demand price: $" + t.demand.peek().price);
        Log.it("\n");
        
        
        Transaction.Request req = new Transaction.QBuy("otrokář", "OtrociAS", "Koláč", 42000 + 430 );
        List<Transaction.Result> ress = t.add(req,77,1234);
        
        for (Transaction.Result res : ress) { Log.it(res); }
        
        Log.it().it(t);
        
    }
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ").append(commodity).append(" --\n");
        
        Row[] sArr = supply.toArray(new Row[0]);
        Row[] dArr = demand.toArray(new Row[0]);
        
        Arrays.sort(sArr, new MaxRowComparator());
        Arrays.sort(dArr, new MaxRowComparator());

        for (Row r : sArr) { sb.append(r.toString()).append("\n"); }
        sb.append("<>\n");
        for (Row r : dArr) { sb.append(r.toString()).append("\n"); }
        
        //sb.append("-------------------\n");
        sb.append(".\n");
        
        return sb.toString();
    }


    
    class MinRowComparator implements Comparator<Row> {
        public int compare(Row r1, Row r2) {
            if (r1.price   < r2.price)   {return -1;}
            if (r1.price   > r2.price)   {return  1;}
            if (r1.transID < r2.transID) {return -1;}    
            return 1;
        }        
    }

    class MaxRowComparator implements Comparator<Row> {
        public int compare(Row r1, Row r2) {
            if (r1.price   < r2.price)   {return  1;}
            if (r1.price   > r2.price)   {return -1;}
            if (r1.transID < r2.transID) {return -1;}    
            return 1;
        }        
    }

    public static class TabuleException extends Exception {
        private String msg;
        public TabuleException(String msg) {
            this.msg = msg;
        }
        @Override
        public String getMessage() {
            return "[TABULE EXCEPTION] : "+msg;
        }
    }
    

    
}
