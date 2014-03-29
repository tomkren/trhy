package cz.tomkren.trhy;

public class Item {

    private Commodity commodity;
    private double num;

    public double getNum() {return num;}
    public Commodity getCommodity() {return commodity;}

    public Item (String comoName, double n) {
        commodity = new Commodity.Basic(comoName);
        num = n;
    }


}
