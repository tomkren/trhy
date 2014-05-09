package cz.tomkren.trhy;


public interface Stuff {

    public Commodity getComo();
    public double getNum ();
    public double addNum(double delta);

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