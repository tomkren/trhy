package cz.tomkren.trhy;

import java.util.*;

public class TrhTester {

    private Trh trh;
    private Random rand;


    public TrhTester (Trh t, Long seed) {
        trh  = t;
        rand = seed == null ? new Random() : new Random(seed);
    }

    public TrhTester (Trh t) {
        this(t, null);
    }

    public boolean sendRandomTrans() {

        InventoryDump beforeDump = trh.getInventoryDump();

        return true;
    }

}
