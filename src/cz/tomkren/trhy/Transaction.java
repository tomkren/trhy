package cz.tomkren.trhy;

/**
 *
 * @author Tomáš Křen
 */
public class Transaction {
    
    public static class Request {
        private TRHead head;

        public TRHead   getHead() {return head;}
        public String   getAID () {return head.getAID();}
        public String   getFID () {return head.getFID();}
        public Comodity getComo() {return head.getComo();}
        
        public Request (String aid, String fid, Comodity c) {
            head = new TRHead(aid, fid, c);
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
    
    public static class TRHead {
        String   agentID;
        String   firmID;
        Comodity comodity;
        
        public TRHead (String aid, String fid, Comodity c) {
            agentID = aid; firmID = fid; comodity = c;
        }
        
        public String   getAID () {return agentID;}
        public String   getFID () {return firmID;}
        public Comodity getComo() {return comodity;}
        
        @Override
        public String toString() {
            return agentID + "\t" + firmID + "\t" + comodity.toString();
        }
    }

    
    
    public static class QBuy extends Request implements Quick, Buy {
        double money;

        public double getMoney() { return money; }  
        
        public QBuy (String aid, String fid, Comodity c, double money) {
            super(aid,fid,c);
            this.money = money;
        }
    }
    
    public static class QSell extends Request implements Quick, Sell {
        double num;
        
        public double getNum() { return num; }
        
        public QSell (String aid, String fid, Comodity c, double num) {
            super(aid,fid,c);
            this.num = num;
        }
    }
    
    public static class SBuy extends Request implements Slow, Buy {
        double money;
        double price;
        
        public double getMoney() { return money; }  
        public double getPrice() { return price; }
        
        public SBuy (String aid, String fid, Comodity c, double money, double price) {
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
        
        public SSell (String aid, String fid, Comodity c, double num, double price) {
            super(aid,fid,c);
            this.num = num;
            this.price = price;
        }
    }
    
    // Zpáva která se vrací při úspěšném dokončení transakce 
    // (z tabule trhu, který jí nějak dále přeposílá agentovi)
    
    // TODO !! potřebujem spešl rezult pro vrácení penez (z Quick transakce, co překročila možnosti trhu)
    
    public static class Result {
        boolean isBuy;     // byla to transakce BUY (či SELL)?
        int     transID;   // transaction ID, aby agent věděl o jakej nákup se jedná, jako alza řekne
        TRHead  head;      // hlavička původní zprávy
        
        double num;       // kolik se přesunulo nakoupilo/prodalo
        double price;     // za jakou to bylo cenu
        
        int startTik;  // tik v kterem byla transakce přidána na burzu
        int finishTik; // tik ve kterém byla transakce uspokojena
        
        public Result (boolean isbuy, int tid, String aid, String fid, Comodity c, double n, double p, int sTik, int fTik) {
            isBuy = isbuy; transID = tid; 
            head = new TRHead(aid, fid, c);
            num = n; price = p; startTik = sTik; finishTik = fTik;
        }

        @Override
        public String toString() {
            return "["+transID+"]\t"+ (isBuy?"BUY":"SELL")+ "\t"+ head.toString() +"\t#"+ num +"\t$"+ price +"\t("+ startTik +","+ finishTik +")";
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
