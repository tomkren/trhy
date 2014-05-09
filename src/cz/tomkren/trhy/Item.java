package cz.tomkren.trhy;

public class Item {

    private Commodity commodity;
    private double num;

    public double getNum() {return num;}
    public Commodity getCommodity() {return commodity;}


    public static Item basic(String cName, double n) {
        return new Item(cName, n);
    }

    public static Item machine(String inputCName, String outputCName, double beta) {
        return new Item(inputCName, outputCName, beta);
    }


    private Item (String comoName, double n) {
        commodity = new Commodity.Basic(comoName);
        num = n;
    }

    private Item (String inputCName, String outputCName, double beta) {
        Machine m = new Machine.Basic(beta, new Commodity.Basic(inputCName) , new Commodity.Basic(outputCName) );
        commodity = new Commodity.Mach(m);
        num = 1;
    }




}
