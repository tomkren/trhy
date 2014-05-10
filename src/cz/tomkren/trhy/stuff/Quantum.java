package cz.tomkren.trhy.stuff;

import cz.tomkren.fishtron.Type;

public class Quantum implements PluralStuff {
    private Type   como;
    private double num;

    public Quantum(Type como, double num) {
        this.como = como;
        this.num = num;
    }
    public Type getComo() {
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