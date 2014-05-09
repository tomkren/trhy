package cz.tomkren.trhy;


public interface Stuff {

    public Commodity getComo();
    public double getNum ();
    public double addNum(double delta);

    default public String getName() {
        return getComo().getName();
    }

    public static Stuff money(double numMoney) {
        return new Basic( new Commodity.Basic("$"), numMoney);
    }

    public static Stuff basic(String comoName, double num) {
        return new Basic( new Commodity.Basic(comoName), num );
    }

    public static Stuff machine(String machineID, String inputCName, String outputCName, double beta) {
        Machine m = new Machine.Basic(machineID, beta, new Commodity.Basic(inputCName) , new Commodity.Basic(outputCName) );

        // todo neměl by bejt spešl sub typ pro stuff typu machine??
        return new Basic( new Commodity.Mach(m) , 1);
    }


    public class Basic implements Stuff {
        private Commodity como;
        private double    num;

        public Basic(Commodity como, double num) {
            this.como = como;
            this.num = num;
        }
        public Commodity getComo() {
            return como;
        }
        public double getNum() {
            return num;
        }
        public double addNum(double delta){
            num += delta;
            return num;
        }
    }


    public class Fail implements Stuff {
        private String msg;

        public Fail(String msg) {
            this.msg = msg;
        }
        @Override
        public double getNum() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Commodity getComo() {
            return null;
        }
        public String getMsg() {
            return msg;
        }
        @Override
        public double addNum(double delta) {
            throw new UnsupportedOperationException();
        }
    }
}
