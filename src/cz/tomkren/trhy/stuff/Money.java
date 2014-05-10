package cz.tomkren.trhy.stuff;


import cz.tomkren.trhy.Commodity;

public class Money extends Quantum {

    public static final Commodity MONEY_COMO = new Commodity.Basic("$");

    public Money(double numMoney) {
        super(MONEY_COMO, numMoney);
    }
}
