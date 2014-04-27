package cz.tomkren.trhy;


public class Trans {

    public static class Head { // ( Agent, his firm(ID), the commodity )
        private String   agentID;
        private String   firmID;
        private Commodity commodity;

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

    public static class Req {
        private Head   head;
        private double price;

        public Head      getHead() {return head;}
        public String    getAID () {return head.getAID();}
        public String    getFID () {return head.getFID();}
        public Commodity getComo() {return head.getComo();}
        public double    getPrice(){return price;}
        public Req (String aid, String fid, String cName, double p) {
            head = new Head(aid, fid, new Commodity.Basic(cName));
            price = p;
        }

        public boolean isQuick() {
            return Double.isInfinite(price); // +inf .. QBUY, -inf .. QSELL
        }
    }

    public static class Buy extends Req {
        private double money;

        public double getMoney() {return money;}
        public Buy (String aid, String fid, String cName, double p, double m) {
            super(aid, fid, cName, p);
            money = m;
        }
        @Override
        public String toString() {
            return "[BUY] ["+getComo()+"] #$"+money+" $"+getPrice()+" ..... "+ getFID()+" ("+getAID()+")";
        }
    }

    public static class Sell extends Req {
        private double num;

        public double getNum() {return num;}
        public Sell (String aid, String fid, String cName, double p, double n) {
            super(aid, fid, cName, p);
            num = n;
        }
        @Override
        public String toString() {
            return "[SELL] ["+getComo()+"] #"+num+" $"+getPrice()+" ..... "+ getFID()+" ("+getAID()+")";
        }
    }



    public static enum Dir    {BUY, SELL}
    public static enum Status {ADD, OK , KO}

    public static class Res {

        private Dir    dir;
        private Status status;
        private double price;
        private double num;
        private double money;

        private Head   head;
        private int    transID;
        private int    startTik;
        private int    finishTik;

        public Res (Dir dir, Status status, double price, double num, double money, Head head, int transID, int startTik, int finishTik) {
            this.dir       = dir      ;
            this.status    = status   ;
            this.price     = price    ;
            this.num       = num      ;
            this.money     = money    ;
            this.head      = head     ;
            this.transID   = transID  ;
            this.startTik  = startTik ;
            this.finishTik = finishTik;
        }
    }

    public static Res mkBuyAdd (double money, double price, Buy buyReq, int transID, int currentTik) {
        double num = money/price;
        return new Res (Dir.BUY, Status.ADD, price, num, money, buyReq.getHead(), transID, currentTik, currentTik);
    }

    public static Res mkBuyKO (double money, Buy buyReq, int transID, int currentTik) {
        double price = Double.POSITIVE_INFINITY; // lze jen při QBUY
        double num   = 0;                        // nic tam neměli
        return new Res (Dir.BUY, Status.KO, price, num, money, buyReq.getHead(), transID, currentTik, currentTik);
    }


}
