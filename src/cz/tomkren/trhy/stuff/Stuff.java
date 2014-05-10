package cz.tomkren.trhy.stuff;

import cz.tomkren.fishtron.Type;

public interface Stuff {

    public Type    getComo();
    public String  dumpKey();
    public double  dumpVal();

    default public String getComoName() {
        return getComo().toString();
    }


    public static Stuff money(double numMoney) {
        return new Money(numMoney);
    }

    public static Stuff quantum(String comoName, double num) {
        return new Quantum( new Type.Const(comoName), num );
    }

    public static Stuff simpleMachine(String machineID, String inputCName, String outputCName, double beta) {
        return new SimpleMachine(machineID, beta, new Type.Arrow(inputCName, outputCName) );
    }


}
