package cz.tomkren.trhy.stuff;

public class Quantum implements PluralStuff {
    private Commodity como;
    private double    num;

    public Quantum(Commodity como, double num) {
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