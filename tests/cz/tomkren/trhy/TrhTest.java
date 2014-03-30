package cz.tomkren.trhy;

import static org.junit.Assert.assertEquals;


public class TrhTest {

    private Trh trh;

    @org.junit.Before
    public void setUp() throws Exception {
        trh = new Trh();
    }

    @org.junit.Test
    public void testGetTik() throws Exception {
        assertEquals(0, trh.getTik());
    }

}
