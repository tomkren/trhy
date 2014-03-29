package cz.tomkren.trhy;

/**
 *
 * @author Tomáš Křen
 */
public class Transaction {

    public static class Head { // ( Agent, his firm(ID), the commodity )
        String   agentID;
        String   firmID;
        Commodity commodity;

        public Head(String aid, String fid, Commodity c) {
            agentID = aid; firmID = fid; commodity = c;
        }

        public String   getAID () {return agentID;}
        public String   getFID () {return firmID;}
        public Commodity getComo() {return commodity;}

        @Override
        public String toString() {
            return agentID + " " + firmID + " " + commodity.toString();
        }
    }

    public static class Request {
        private Head head;

        public Head getHead() {return head;}
        public String   getAID () {return head.getAID();}
        public String   getFID () {return head.getFID();}
        public Commodity getComo() {return head.getComo();}
        
        public Request (String aid, String fid, Commodity c) {
            head = new Head(aid, fid, c);
        }
    }
    
    public static interface Buy {
        double getMoney();
    }

    public static interface Sell {
        double getNum();
    }
    
    public static interface Quick {}
    
    public static interface Slow {
        double getPrice();
    }
    


    
    
    public static class QBuy extends Request implements Quick, Buy {
        double money;

        public double getMoney() { return money; }  
        
        public QBuy (String aid, String fid, Commodity c, double money) {
            super(aid,fid,c);
            this.money = money;
        }
    }
    
    public static class QSell extends Request implements Quick, Sell {
        double num;
        
        public double getNum() { return num; }
        
        public QSell (String aid, String fid, Commodity c, double num) {
            super(aid,fid,c);
            this.num = num;
        }
    }
    
    public static class SBuy extends Request implements Slow, Buy {
        double money;
        double price;
        
        public double getMoney() { return money; }  
        public double getPrice() { return price; }
        
        public SBuy (String aid, String fid, Commodity c, double money, double price) {
            super(aid,fid,c);
            this.money = money;
            this.price = price;
        }
    }
    
    public static class SSell extends Request implements Slow, Sell {
        double num;
        double price;
        
        public double getNum  () { return num;   }
        public double getPrice() { return price; }
        
        public SSell (String aid, String fid, Commodity c, double num, double price) {
            super(aid,fid,c);
            this.num = num;
            this.price = price;
        }
    }
    
    // Zpáva která se vrací při úspěšném dokončení transakce 
    // (z tabule trhu, který jí nějak dále přeposílá agentovi)
    
    // TODO !! potřebujem spešl rezult pro vrácení penez (z Quick transakce, co překročila možnosti trhu)
    
    public static class Result {

        public static enum ResultType {BUY, SELL, NO_Q_BUY, NO_Q_SELL};
        ResultType resultType;

        int  transID; // transaction ID, aby agent věděl o jakej nákup se jedná, jako alza řekne
        Head head;    // Message head (of the transaction)
        
        double num;       // How many commodities this agent gets. (Used in BUY,  NO_Q_SELL - otherwise 0.)
        double money;     // How many money this agent gets.       (Used in SELL, NO_Q_BUY  - otherwise 0.)
        Double price;     // For what price.

        int startTik;  // tik v kterem byla transakce přidána na burzu
        int finishTik; // tik ve kterém byla transakce uspokojena
        
        public Result (ResultType rt, int tid, String aid, String fid, Commodity c, double n, double m, double p, int sTik, int fTik) {
            resultType = rt; transID = tid;
            head = new Head(aid, fid, c);
            num = n; money = m; price = p; startTik = sTik; finishTik = fTik;
        }

        @Override
        public String toString() {
            String priceStr = price == null ? "<NO-PRICE>" : "<$"+ price +">";
            return "["+transID+"] "+resultType.toString()+" "+priceStr+" "+head.toString()+" #"+num+" #$"+money+" ("+ startTik +","+ finishTik +")";
        }
        
        
    }
    
    
    
    public static class CheckResult { // jakoby bool s msg pro false
        boolean ok;
        String msg;
        
        public CheckResult(boolean isOk, String m){
            ok  = isOk;
            msg = m;
        } 
    }
    
    public static final CheckResult OK = new CheckResult(true, null);
    public static CheckResult ko(String msg){
        return new CheckResult(false, msg);
    }
    
}
