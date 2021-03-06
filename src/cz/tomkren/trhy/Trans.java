package cz.tomkren.trhy;


import cz.tomkren.fishtron.Type;

public class Trans {

    public static Req mkSlowBuy   (String aid, String fid, String c, double money, double price) { return new  Buy(aid, fid, c, price, money); }
    public static Req mkSlowSell  (String aid, String fid, String c, double num  , double price) { return new Sell(aid, fid, c, price, num  ); }

    public static Req mkQuickBuy  (String aid, String fid, String c, double money) { return  mkSlowBuy(aid, fid, c, money, Double.POSITIVE_INFINITY); }
    public static Req mkQuickSell (String aid, String fid, String c, double num  ) { return mkSlowSell(aid, fid, c, num  , Double.NEGATIVE_INFINITY); }


    public static class Head { // ( Agent, his firm(ID), the commodity )
        private String agentID;
        private String firmID;
        private Type   commodity;

        public Head(String aid, String fid, Type c) {
            agentID = aid; firmID = fid; commodity = c;
        }
        public String   getAID () {return agentID;}
        public String   getFID () {return firmID;}
        public Type     getComo() {return commodity;}
        @Override
        public String toString() {
            return agentID + " " + firmID + " " + commodity.toString();
        }
    }

    public static class Req {
        private Head   head;
        private double price;

        public Head   getHead     () {return head;}
        public String getAID      () {return head.getAID();}
        public String getFID      () {return head.getFID();}
        public Type   getComo     () {return head.getComo();}
        public String getComoName () {return head.getComo().toString();}
        public double getPrice    () {return price;}
        public Req (String aid, String fid, String cName, double p) {
            head = new Head(aid, fid, new Type.Const(cName) );
            price = p;
        }

        public String getPriceStr() {
            return isQuick() ? "[QUICK]" : "$"+getPrice() ;
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
            return "[BUY]  ["+getComo()+"] #$"+money+" "+getPriceStr()+" ..... "+ getFID()+" ("+getAID()+")";
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
            return "[SELL] ["+getComo()+"] #"+num+" "+getPriceStr()+" ..... "+ getFID()+" ("+getAID()+")";
        }
    }



    public static enum Dir           {BUY, SELL}
    public static enum Status        {ADD, EXCHANGE, FAIL}
    public static enum EffectType {ADD_MONEY, ADD_COMMODITY, NOTHING}

    private static class ResEffect {
        private EffectType type;
        private double        val;
        public ResEffect (EffectType type, double val) { this.type = type; this.val = val; }
        public EffectType getType() {return type;}
        public double getVal() {return val;}
    }

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

        private ResEffect resEffect;

        public Res (Dir dir, Status status, double price, double num, double money, Head head, int transID, int startTik, int finishTik) {
            this.dir       = dir       ;
            this.status    = status    ;
            this.price     = price     ;
            this.num       = num       ;
            this.money     = money     ;
            this.head      = head      ;
            this.transID   = transID   ;
            this.startTik  = startTik  ;
            this.finishTik = finishTik ;

            resEffect = getResEffect();
        }



        public EffectType getEffectType() {return resEffect.getType();}
        public double getEffectVal()      {return resEffect.getVal(); }

        public String getFID()  {return head.getFID(); }
        public Type   getComo() {return head.getComo();}

        private ResEffect getResEffect () {
            switch (status) {
                case EXCHANGE : switch (dir) { case BUY  : return new ResEffect(EffectType.ADD_COMMODITY , num   );
                                               case SELL : return new ResEffect(EffectType.ADD_MONEY     , money ); }
                case FAIL     : switch (dir) { case BUY  : return new ResEffect(EffectType.ADD_MONEY     , money );
                                               case SELL:  return new ResEffect(EffectType.ADD_COMMODITY , num   ); }
                default:                                   return new ResEffect(EffectType.NOTHING       , 0     );
            }
        }

        @Override
        public String toString() {
            return "[ "+ dir.toString() +" "+ status.toString() +" #$"+ money +" $"+price+" #"+num +"] => "+getEffectType()+" "+getEffectVal();
        }
    }

    public static Res mkBuyAddResult(double money, double price, Buy buyReq, int transID, int currentTik) {
        double num = money/price;
        return new Res(Dir.BUY, Status.ADD, price, num, money, buyReq.getHead(), transID, currentTik, currentTik);
    }

    public static Res mkBuyFailResult(double money, Buy buyReq, int transID, int currentTik) {
        double price = Double.POSITIVE_INFINITY; // lze jen při QBUY
        double num   = 0;                        // nic tam neměli
        return new Res(Dir.BUY, Status.FAIL, price, num, money, buyReq.getHead(), transID, currentTik, currentTik);
    }

    public static Res mkSellAddResult(double num, double price, Sell sellReq, int transID, int currentTik) {
        double money = num*price;
        return new Res(Dir.SELL, Status.ADD, price, num, money, sellReq.getHead(), transID, currentTik, currentTik);
    }

    public static Res mkSellFailResult(double num, Sell sellReq, int transID, int currentTik) {
        double price = Double.NEGATIVE_INFINITY; // lze jen při QSELL
        double money = 0;                        // nikdo to nechtěl
        return new Res(Dir.SELL, Status.FAIL, price, num, money, sellReq.getHead(), transID, currentTik, currentTik);
    }


    public static class CheckStatus { // jakoby bool s msg pro false
        boolean ok;
        String msg;

        public CheckStatus(boolean isOk, String m){
            ok  = isOk;
            msg = m;
        }

        public String getMsg() {
            return msg;
        }

        public boolean isOk () {
            return ok;
        }


    }

    public static final CheckStatus OK = new CheckStatus(true, null);
    public static CheckStatus ko(String msg){
        return new CheckStatus(false, msg);
    }

}
