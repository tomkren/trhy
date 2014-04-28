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
            
    private class Row {
        
        int    transID;   // transaction ID
        String agentID;   // agent ID, abych věděl koho informovat
        String firmID;    // firm ID
        double price;     // price
        double num;       // number of items
        int    tik;       // tik zadání
        
        public Row (int tid, String aid, String fid, double p, double n, int t) {
            transID = tid; agentID = aid; firmID  = fid; price = p; num = n; tik = t;
        }

        public Trans.Head getHead () {
            return new Trans.Head(agentID, firmID, commodity);
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

    private void addSupplyRow (int transID, String agentID, String firmID, double price, double num, int tik) {
        supply.add(new Row(transID, agentID, firmID, price, num, tik));
    }

    private void addDemandRow (int transID, String agentID, String firmID, double price, double num, int tik) {
        demand.add(new Row(transID, agentID, firmID, price, num, tik));
    }

    public Tabule(Commodity commodity) {
        this.commodity = commodity;
        int initialCapacity = 11; //11 je prej default
        supply = new PriorityQueue<Row>(initialCapacity, new MinRowComparator()); 
        demand = new PriorityQueue<Row>(initialCapacity, new MaxRowComparator());
    }

    private final static Transaction.Result.ResultType BUY       = Transaction.Result.ResultType.BUY;
    private final static Transaction.Result.ResultType SELL      = Transaction.Result.ResultType.SELL;
    private final static Transaction.Result.ResultType NO_Q_BUY  = Transaction.Result.ResultType.NO_Q_BUY;
    private final static Transaction.Result.ResultType NO_Q_SELL = Transaction.Result.ResultType.NO_Q_SELL;

//......................................

    public List<Trans.Res> add (Trans.Req req, int transID, int currentTik) {
        if (req instanceof Trans.Buy) { return  addBuy(new  BuyOpts((Trans.Buy) req, transID, currentTik)); }
        if (req instanceof Trans.Sell){ return addSell(new SellOpts((Trans.Sell)req, transID, currentTik)); }
        return null;
    }


    private List<Trans.Res> addBuy (BuyOpts buyOpts) {
        List<Trans.Res> ret = new LinkedList<Trans.Res>();

        Trans.Buy buyReq = buyOpts.buyReq;
        double myPrice = buyReq.getPrice();
        double myMoney = buyReq.getMoney();

        while (myMoney > 0 && !supply.isEmpty()) {

            Row supplyRow = supply.peek();

            if (supplyRow.getPrice() <= myPrice) {
                myMoney = performBuyExchangeWithSupplyRow(ret, supplyRow, myMoney, buyOpts);
            } else {
                addToDemand(ret, myPrice, myMoney, buyOpts);
                return ret;
            }
        }

        if (myMoney > 0) {
            if (buyReq.isQuick()) {
                addBuyFailResult(ret, myMoney, buyOpts);
            } else {
                addToDemand(ret, myPrice, myMoney, buyOpts);
            }
        }

        return ret;
    }

    private List<Trans.Res> addSell (SellOpts sellOpts) {
        List<Trans.Res> ret = new LinkedList<Trans.Res>();

        Trans.Sell sellReq = sellOpts.sellReq;
        double myPrice = sellReq.getPrice();
        double myNum   = sellReq.getNum();

        while (myNum > 0 && !demand.isEmpty()) {

            Row demandRow = demand.peek();

            if (demandRow.getPrice() >= myPrice) {
                myNum = performSellExchangeWithDemandRow(ret, demandRow, myNum, sellOpts);
            } else {
                addToSupply(ret, myPrice, myNum, sellOpts);
                return ret;
            }
        }

        if (myNum > 0) {
            if (sellReq.isQuick()) {
                addSellFailResult(ret, myNum, sellOpts);
            } else {
                addToSupply(ret, myPrice, myNum, sellOpts);
            }
        }

        return ret;
    }

    private void addToDemand (List<Trans.Res> ret, double myPrice, double myMoney, BuyOpts buyOpts) {
        String aid = buyOpts.buyReq.getAID();
        String fid = buyOpts.buyReq.getFID();
        Row newRow = new Row(buyOpts.transID, aid, fid, myPrice, myMoney/myPrice, buyOpts.currentTik);

        ret.add( Trans.mkBuyAddResult(myMoney, myPrice, buyOpts.buyReq, buyOpts.transID, buyOpts.currentTik) );
        demand.add(newRow);
    }

    private void addToSupply (List<Trans.Res> ret, double myPrice, double myNum, SellOpts sellOpts) {
        String aid = sellOpts.sellReq.getAID();
        String fid = sellOpts.sellReq.getFID();
        Row newRow = new Row(sellOpts.transID, aid, fid, myPrice, myNum, sellOpts.currentTik);

        ret.add( Trans.mkSellAddResult(myNum, myPrice, sellOpts.sellReq, sellOpts.transID, sellOpts.currentTik) );
        supply.add(newRow);
    }

    private void addBuyFailResult (List<Trans.Res> ret, double myMoney, BuyOpts buyOpts) {
        ret.add( Trans.mkBuyFailResult(myMoney, buyOpts.buyReq, buyOpts.transID, buyOpts.currentTik) );
    }

    private void addSellFailResult (List<Trans.Res> ret, double myNum, SellOpts sellOpts) {
        ret.add( Trans.mkSellFailResult(myNum, sellOpts.sellReq, sellOpts.transID, sellOpts.currentTik) );
    }

    private double performBuyExchangeWithSupplyRow (List<Trans.Res> ret, Row row, double myMoney, BuyOpts buyOpts) {

        Trans.Buy buyReq = buyOpts.buyReq ;
        int transID      = buyOpts.transID;
        int currentTik   = buyOpts.currentTik;

        double  rowPrice    = row.getPrice();
        boolean isOverflow  = myMoney > row.getValue();                      // request nebude plně uspokojen tímto řádkem
        double  numToBuy    = isOverflow ? row.getNum() : myMoney/rowPrice;  // kolik kusu tedy koupím
        double  moneyForBuy = numToBuy * rowPrice;                           // .. a kolik mě to bude stát

        if (isOverflow) {
            supply.poll();
        } else {
            row.decreaseNum(numToBuy);
            if (row.getNum() <= 0) {
                supply.poll();
            }
        }

        ret.add( new Trans.Res(Trans.Dir.BUY,  Trans.Status.OK, rowPrice, numToBuy, moneyForBuy, buyReq.getHead(), transID, currentTik,        currentTik) );
        ret.add( new Trans.Res(Trans.Dir.SELL, Trans.Status.OK, rowPrice, numToBuy, moneyForBuy, row.getHead()   , transID, row.getStartTik(), currentTik) );

        return myMoney - moneyForBuy;
    }

    private double performSellExchangeWithDemandRow (List<Trans.Res> ret, Row row, double myNum, SellOpts sellOpts) {

        Trans.Sell sellReq = sellOpts.sellReq ;
        int transID        = sellOpts.transID;
        int currentTik     = sellOpts.currentTik;

        double  rowPrice     = row.getPrice();
        boolean isOverflow   = myNum > row.getNum();               // request nebude plně uspokojen tímto poptávkovým řádkem
        double  numToSell    = isOverflow ? row.getNum() : myNum;  // ... kolik kusu prodám
        double  moneyForSell = numToSell * rowPrice;               // ... a kolik za to dostanu

        if (isOverflow) {
            demand.poll();
        } else {
            row.decreaseNum(numToSell);
            if (row.getNum() <= 0) {
                demand.poll();
            }
        }

        ret.add( new Trans.Res(Trans.Dir.SELL, Trans.Status.OK, rowPrice, numToSell, moneyForSell, sellReq.getHead(), transID, currentTik,        currentTik) );
        ret.add( new Trans.Res(Trans.Dir.BUY , Trans.Status.OK, rowPrice, numToSell, moneyForSell, row.getHead()    , transID, row.getStartTik(), currentTik) );

        return myNum - numToSell;
    }

    public static class BuyOpts {
        private Trans.Buy buyReq;
        private int       transID;
        private int       currentTik;

        public BuyOpts(Trans.Buy buyReq, int transID, int currentTik) {
            this.buyReq = buyReq;
            this.transID = transID;
            this.currentTik = currentTik;
        }
    }

    public static class SellOpts {
        private Trans.Sell sellReq;
        private int       transID;
        private int       currentTik;

        public SellOpts(Trans.Sell sellReq, int transID, int currentTik) {
            this.sellReq = sellReq;
            this.transID = transID;
            this.currentTik = currentTik;
        }
    }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public List<Transaction.Result> add_pokus2 (Transaction.Request tr, int transID , int currentTik) {
        boolean  isBuy   = tr instanceof Transaction.Buy;
        boolean  isQuick = tr instanceof Transaction.Quick;
        double   price   = isQuick ? 0 : ((Transaction.Slow)tr).getPrice() ;
        double   money   = isBuy ? ((Transaction.Buy)tr).getMoney() : 0;
        double   num     = isBuy ? 0 : ((Transaction.Sell)tr).getNum() ;
        List<Transaction.Result> acc = new LinkedList<Transaction.Result>();

        return add_rec(isBuy, isQuick, price, money, num, tr, transID, currentTik, acc);
    }

    private List<Transaction.Result> add_rec (  boolean                   isBuy           ,
                                                boolean                   isQuick         ,
                                                double                    price           ,
                                                double                    money           ,
                                                double                    num             ,
                                                Transaction.Request       tre             ,
                                                int                       transID         ,
                                                int                       currentTik      ,
                                                List<Transaction.Result>  acc             ) {
        // better safe than sorry
        if (isBuy) { if (money <= 0) { return acc; } }
        else       { if (num   <= 0) { return acc; } }

        PriorityQueue<Row> pullQueue = isBuy ? supply : demand;
        PriorityQueue<Row> pushQueue = isBuy ? demand : supply;

        if (pullQueue.isEmpty()) {
            if (isQuick) { addQuickFail(acc, isBuy, tre, num, money, transID, currentTik);}
            else         { addSlowRow(pushQueue, tre, price  , num , transID, currentTik);}
        } else { // pull row exists
            Row row = pullQueue.peek();

            // vyndáme důležité položky z "nejvýhodnějšího" řádku
            double rowValue = row.getValue();
            double rowPrice = row.getPrice();
            double rowNum   = row.getNum();

            // rozhodneme zda se řádek použije (nakoupime/prodame s nim) či ne
            boolean isRowPriceOK = isQuick || // quick je automaticky OK, slow musí být prozkoumána
                                           (isBuy ?  rowPrice <= price   // pokud nakupuju, snesu jen nižší cenu
                                                  :  rowPrice >= price); // pokud prodavam, tak jedine za víc

            // tady může být price == 0 pro QUICK
            boolean isRowBad = isBuy ? rowPrice > price : rowPrice < price;

            if (isQuick) {

            } else { // isSlow
                if (isRowPriceOK) {

                } else { // isSlow AND row price KO
                    addSlowRow(pushQueue, tre, price  , num , transID, currentTik);
                }
            }


        }

        return acc;

        /*

        // todo : udelany debilne
        // ...
        // ...
        // ...

        Row row = null;
        double rowValue = 0, rowPrice = 0, rowNum = 0;
        boolean isRowBad; // bud ze row vubec neni, nebo ze je moc drahej

        if (isPullEmpty) {
            isRowBad = true; // protoze row vubec neni
        } else {
            // vyndáme důležité položky z "nejvýhodnějšího" řádku
            row      = pullQueue.peek();
            rowValue = row.getValue();
            rowPrice = row.getPrice();
            rowNum   = row.getNum();

            isRowBad = isBuy ? rowPrice > price : rowPrice < price;
        }

        num = isBuy ? money/rowPrice : num;


        // pro pomalou transakcí s moc narocnou cenou (oproti nej radku row)
        // přidáme nový řádek a končíme
        if (!isQuick && isRowBad) {
            // pro jistotu ještě zde kontrola kladnosti ceny a mnozstvi
            if (price <= 0 || num <= 0) {return acc;}
            // přidáme řádek
            pushQueue.add(new Row(transID, tre.getAID(), tre.getFID(), price  , num , currentTik));
            // a žádné další nákupy se zatím nekonají
            // todo : dava smysl pridat do acc result o pridani radku
            return acc;
        }

        // isOverflow znamená, že request nebude plně uspokojen tímto řádkem
        boolean isOverflow = num > rowNum;
        // kolik kusu tedy prohodit
        double numToChange = isOverflow ? rowNum : num;
        // .. a kolik za to bude penez
        double moneyToChange = numToChange*rowPrice;

        // budeme odebírat řádek, případně ho jen upravovat
        if (isOverflow) {
            pullQueue.poll();
        } else {
            row.decreaseNum(numToChange);
            if (row.getNum() <= 0) { // jsou to doubly, tak by to mohlo jit zaokrouhlením pod
                pullQueue.poll(); //řádek už je prázdný, vyhodíme
            }
        }

        //results pro probehle transakce
        acc.add(new Transaction.Result(isBuy?BUY:SELL, transID,      tre.getAID(), tre.getFID(), commodity, numToChange, moneyToChange , rowPrice, currentTik,        currentTik ));
        acc.add(new Transaction.Result(isBuy?SELL:BUY, row.getTID(), row.getAID(), row.getFID(), commodity, numToChange, moneyToChange , rowPrice, row.getStartTik(), currentTik ));

        if (isOverflow) { return add_rec(isBuy, isQuick, price, isBuy ? money - rowValue : 0, isBuy ? 0 : num - rowNum, tre, transID, currentTik, acc); }
        else            { return acc; }

        */
    }

    private void addQuickFail (List<Transaction.Result> acc, boolean isBuy, Transaction.Request tre, double num, double money,int transID, int currentTik) {
        Transaction.Result.ResultType resType = isBuy ? NO_Q_BUY : NO_Q_SELL ;
        Transaction.Result quickFail = new Transaction.Result(
            resType, transID, tre.getAID(), tre.getFID(), commodity, num, money, 0, currentTik, currentTik);
        acc.add(quickFail);
    }

    private void addSlowRow (PriorityQueue<Row> pushQueue, Transaction.Request tre, double price  , double num , int transID, int currentTik) {
        if (price <= 0 || num <= 0) {return;}
        Row slowRow = new Row(transID, tre.getAID(), tre.getFID(), price  , num , currentTik);
        pushQueue.add(slowRow); // todo : dava smysl pridat do acc result o pridani radku
    }




// .....................................





    public List<Transaction.Result> add_old (Transaction.Request tr, int transID , int currentTik) {
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

        t.addSupplyRow(3, "žid",   "Koloniál",  44, 3   ,10);
        t.addSupplyRow(1, "pekař", "Pekař&Syn", 42, 10  ,1);
        t.addSupplyRow(2, "pekař2", "Pekař&Syn2", 43, 110 ,5);

        t.addDemandRow(30, "otrokář", "UKsro",    41  ,   3, 100);
        t.addDemandRow(10, "žid",     "Koloniál", 40  ,  10, 20 );
        t.addDemandRow(20, "otrokář", "UKsro",    41.5, 110, 3  );
        
        
        Log.it().it(t);
        
        Log.it("Best supply price: $" + t.supply.peek().price);
        Log.it("Best demand price: $" + t.demand.peek().price);
        Log.it("\n");
        
        
        Transaction.Request req = new Transaction.QBuy("otrokář", "OtrociAS", "Koláč", 42000 + 430 );
        List<Transaction.Result> ress = t.add_old(req, 77, 1234);
        
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
