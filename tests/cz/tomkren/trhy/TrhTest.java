package cz.tomkren.trhy;

import cz.tomkren.trhy.helpers.Log;
import cz.tomkren.trhy.helpers.TrhTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TrhTest {

    private Trh trh;
    private TrhTester trhTester;

    @org.junit.Before
    public void setUp() throws Exception {

        trh = new Trh();
        trhTester = new TrhTester(trh);

        try {
            trh.addFirm("Penuel Katz" , Firm.Examples.mkKolonialKatz());
            //trh.addFirm("Penuel Katz Fake",Firm.Examples.mkKolonialKatz()); // má vyhodit výjimku že už se tak něco jmenuje
            trh.addFirm("Václav Rolný", Firm.Examples.mkPoleAS());
        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> " + e.getMessage());
        }
    }

    @org.junit.Test
    public void testGetTik() throws Exception {
        assertEquals(2, trh.getTik());
    }

    @org.junit.Test
    public void randomTransaction() {
        assertTrue(trhTester.sendRandomTrans(false));
    }

    @org.junit.Test
    public void manyRandomTransactions() {
        assertTrue(trhTester.sendRandomTrans(1000, false));
    }

}
