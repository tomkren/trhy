package cz.tomkren.trhy;

import java.util.*;

/**
 *
 * @author Tomáš Křen
 */


public class Tabule {
    
    private Comodity comodity;

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
            return "$"+price+" ... "+num+" ks ... "+firmID+"("+agentID+")";
        }
    }

    
    public Tabule(Comodity comodity) {
        this.comodity = comodity;
        int initialCapacity = 11; //11 je prej default
        supply = new PriorityQueue<Row>(initialCapacity, new MinRowComparator()); 
        demand = new PriorityQueue<Row>(initialCapacity, new MaxRowComparator());
    }
    
    public List<Transaction.Result> add (Transaction.Request tr, int transID , int currentTik) {
        // TODO rozdelané
      
        if (tr instanceof Transaction.QBuy) {
            Transaction.QBuy qbuy = (Transaction.QBuy)tr;
            double moneyToSpend = qbuy.getMoney();
            return buyZaTrzniCenu(moneyToSpend, tr, transID, currentTik);
        }
        
        // ... ostatni připady            

        
        return null;
    }
    
    
    private List<Transaction.Result> buyZaTrzniCenu (double moneyToSpend, Transaction.Request tr, int transID, int currentTik) {
        return buyZaTrzniCenu_rec(moneyToSpend, tr, transID, currentTik, new LinkedList<Transaction.Result>());
    }
    
    private List<Transaction.Result> buyZaTrzniCenu_rec (double moneyToSpend, Transaction.Request tre, int transID, int currentTik, List<Transaction.Result> acc) {        
        
        // TODO : čečit esli nahodou neni prázdná tabulka!!!!
        
        if (supply.isEmpty()) { return acc; } // TODO lépe, musíme poslat spešl rezult kterej vrátí peníze
        
        if (moneyToSpend <= 0) { return acc; } // radši pojistka

        Row    row      = supply.peek();
        double rowValue = row.getValue();
        double rowPrice = row.getPrice();
        double rowNum   = row.getNum();

        boolean preteklo = (moneyToSpend > rowValue);

        double numToBuy = preteklo ? rowNum : moneyToSpend/rowPrice ;

        if (preteklo) {
            supply.poll();
        } else {
            row.decreaseNum(numToBuy);
            if (row.getNum() <= 0) { // jsou to doubly, tak by to mohlo jit zaokrouhlenim pod
                supply.poll(); //řádek už je prázdný, vyhodímež
            }
        }

        acc.add(new Transaction.Result(true,  transID,      tre.getAID(), tre.getFID(), comodity, numToBuy, rowPrice, currentTik,        currentTik ));
        acc.add(new Transaction.Result(false, row.getTID(), row.getAID(), row.getFID(), comodity, numToBuy, rowPrice, row.getStartTik(), currentTik ));

        if (preteklo) {
            return buyZaTrzniCenu_rec(moneyToSpend-rowValue, tre, transID, currentTik, acc);
        } else {
            return acc;
        }
    }
    
 
    
    public Row bestDemand () { return demand.peek(); }
    public Row bestSupply () { return supply.peek(); }
    
    public double buyMarketPrice  () { return bestSupply().price; }
    public double sellMarketPrice () { return bestSupply().price; }
    
    public static void main (String[] args) {
        Log.it("Tabule main, hello!");
        
        Comodity kolac = new BasicComodity("Koláč");
        
        Tabule t = new Tabule(kolac);
               
        t.supply.add(new Row(3, "žid",   "Koloniál",  44, 3   ,10));
        t.supply.add(new Row(1, "pekař", "Pekař&Syn", 42, 10  ,1));
        t.supply.add(new Row(2, "pekař2", "Pekař&Syn2", 43, 110 ,5));

        t.demand.add(new Row(30, "otrokář", "UKsro",    41  ,   3, 100));
        t.demand.add(new Row(10, "žid",     "Koloniál", 40  ,  10, 20));
        t.demand.add(new Row(20, "otrokář", "UKsro",    41.5, 110, 3));
        
        
        Log.it(t);
        
        Log.it("Best supply price: $" + t.supply.peek().price);
        Log.it("Best demand price: $" + t.demand.peek().price);
        Log.it("\n");
        
        
        Transaction.Request req = new Transaction.QBuy("otrokář", "OtrociAS", kolac, 420 + 430 );
        List<Transaction.Result> ress = t.add(req,77,1234);
        
        for (Transaction.Result res : ress) {
            Log.it(res);
        }
        
        Log.it(t);
        
    }
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n-- ").append(comodity).append(" ----------\n");
        
        Row[] sArr = supply.toArray(new Row[0]);
        Row[] dArr = demand.toArray(new Row[0]);
        
        Arrays.sort(sArr, new MaxRowComparator());
        Arrays.sort(dArr, new MaxRowComparator());

        for (Row r : sArr) { sb.append(r.toString()).append("\n"); } 
        sb.append("\n");
        for (Row r : dArr) { sb.append(r.toString()).append("\n"); }
        
        sb.append("-------------------\n");
        
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
