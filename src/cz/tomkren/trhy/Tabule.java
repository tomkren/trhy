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

    public static enum RowType {SUPPLY, DEMAND}

    private class Row implements InventoryDump.Dumpable {
        
        private int     transID;   // transaction ID
        private String  agentID;   // agent ID, abych věděl koho informovat
        private String  firmID;    // firm ID
        private double  price;     // price
        private double  num;       // number of items
        private int     tik;       // tik zadání
        private RowType rowType;
        
        public Row (RowType type, int tid, String aid, String fid, double p, double n, int t) {
            rowType = type; transID = tid; agentID = aid; firmID  = fid; price = p; num = n; tik = t;
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
            return "$"+price+" ... "+num+" ks ... "+getFID()+" ("+getAID()+") [tid "+getTID()+" tik "+ tik +"]";
        }

        @Override
        public Map.Entry<String, Double> dump() {
            boolean isSupply = rowType == RowType.SUPPLY ;
            String key = isSupply ? commodity.getName() : "$"       ;
            Double val = isSupply ? getNum()            : getValue();
            return new AbstractMap.SimpleImmutableEntry<String, Double>(key, val);
        }
    }
    private void addSupplyRow (int transID, String agentID, String firmID, double price, double num, int tik) {
        supply.add(new Row(RowType.SUPPLY,transID, agentID, firmID, price, num, tik));
    }
    private void addDemandRow (int transID, String agentID, String firmID, double price, double num, int tik) {
        demand.add(new Row(RowType.DEMAND,transID, agentID, firmID, price, num, tik));
    }

    public Tabule(Commodity commodity) {
        this.commodity = commodity;
        int initialCapacity = 11; //11 je prej default
        supply = new PriorityQueue<Row>(initialCapacity, new MinRowComparator()); 
        demand = new PriorityQueue<Row>(initialCapacity, new MaxRowComparator());
    }

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
        Row newRow = new Row(RowType.DEMAND,buyOpts.transID, aid, fid, myPrice, myMoney/myPrice, buyOpts.currentTik);

        ret.add( Trans.mkBuyAddResult(myMoney, myPrice, buyOpts.buyReq, buyOpts.transID, buyOpts.currentTik) );
        demand.add(newRow);
    }

    private void addToSupply (List<Trans.Res> ret, double myPrice, double myNum, SellOpts sellOpts) {
        String aid = sellOpts.sellReq.getAID();
        String fid = sellOpts.sellReq.getFID();
        Row newRow = new Row(RowType.SUPPLY ,sellOpts.transID, aid, fid, myPrice, myNum, sellOpts.currentTik);

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

        ret.add( new Trans.Res(Trans.Dir.BUY,  Trans.Status.EXCHANGE, rowPrice, numToBuy, moneyForBuy, buyReq.getHead(), transID, currentTik,        currentTik) );
        ret.add( new Trans.Res(Trans.Dir.SELL, Trans.Status.EXCHANGE, rowPrice, numToBuy, moneyForBuy, row.getHead()   , transID, row.getStartTik(), currentTik) );

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

        ret.add( new Trans.Res(Trans.Dir.SELL, Trans.Status.EXCHANGE, rowPrice, numToSell, moneyForSell, sellReq.getHead(), transID, currentTik,        currentTik) );
        ret.add( new Trans.Res(Trans.Dir.BUY , Trans.Status.EXCHANGE, rowPrice, numToSell, moneyForSell, row.getHead()    , transID, row.getStartTik(), currentTik) );

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



    public InventoryDump getInventoryDump() {
        Collection<? extends InventoryDump.Dumpable> supplyCol = supply;
        Collection<? extends InventoryDump.Dumpable> demandCol = demand;

        InventoryDump ret = new InventoryDump(supplyCol);
        ret.add(new InventoryDump(demandCol));

        return ret;
    }
    
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


        Log.it();
        Log.it(t);
        
        Log.it("Best supply price: $" + t.supply.peek().price);
        Log.it("Best demand price: $" + t.demand.peek().price);
        Log.it("\n");
        
        
        Trans.Req req = Trans.mkQuickBuy("otrokář", "OtrociAS", "Koláč", 42000 + 430 );
        List<Trans.Res> ress = t.add(req, 77, 1234);
        
        for (Trans.Res res : ress) { Log.it(res); }

        Log.it();
        Log.it(t);
        
    }
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ").append(commodity).append(" --\n");
        
        Row[] sArr = supply.toArray(new Row[supply.size()]);
        Row[] dArr = demand.toArray(new Row[demand.size()]);
        
        Arrays.sort(sArr, new MaxRowComparator());
        Arrays.sort(dArr, new MaxRowComparator());

        for (Row r : sArr) { sb.append(r.toString()).append("\n"); }
        sb.append("<>\n");
        for (Row r : dArr) { sb.append(r.toString()).append("\n"); }

        //sb.append("...").append( getInventoryDump() );

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


}
