package cz.tomkren.trhy.stuff;


import cz.tomkren.fishtron.Type;

public class Money extends Quantum {
    public static final Type MONEY_COMO = new Type.Const("$");
    public Money(double numMoney) {
        super(MONEY_COMO, numMoney);
    }
}
