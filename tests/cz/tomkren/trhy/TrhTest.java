package cz.tomkren.trhy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TrhTest {

    private Trh trh;
    private TrhTester trhTester;

    @org.junit.Before
    public void setUp() throws Exception {

        Firm kolonial = new Firm(
                "Koloniál Katz", //Penuel Katz
                new Item[]{
                        new Item("$",100000),
                        new Item("Work",5),
                        new Item("Flour",5000),
                        new Item("Pie",100)
                });

        Firm poleAS = new Firm(
                "Pole a.s.",
                new Item[]{
                        new Item("$",1000),
                        new Item("Work",1000),
                        new Item("Flour",5000)
                });



        trh = new Trh();
        trhTester = new TrhTester(trh);

        try {

            trh.addAgentsFirm("Penuel Katz",kolonial);
            //trh.addAgentsFirm("Penuel Katz Fake",kolonial); // má vyhodit výjimku že už se tak něco jmenuje
            trh.addAgentsFirm("Václav Rolný",poleAS);


        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> "+e.getMessage());
        }
    }

    @org.junit.Test
    public void testGetTik() throws Exception {
        assertEquals(0, trh.getTik());
    }

    @org.junit.Test
    public void randomTransaction() {
        assertTrue(trhTester.sendRandomTrans());

    }

}
