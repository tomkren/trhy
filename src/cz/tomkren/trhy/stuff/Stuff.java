package cz.tomkren.trhy.stuff;

public interface Stuff {

    public Commodity getComo();
    public String    dumpKey();
    public double    dumpVal();

    default public String getComoName() {
        return getComo().getName();
    }


    public static Stuff money(double numMoney) {
        return new Money(numMoney);
    }

    public static Stuff quantum(String comoName, double num) {
        return new Quantum( new Commodity.Basic(comoName), num );
    }

    public static Stuff simpleMachine(String machineID, String inputCName, String outputCName, double beta) {
        return new SimpleMachine(machineID, beta, new Commodity.Basic(inputCName), new Commodity.Basic(outputCName) );
    }


}
