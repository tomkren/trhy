package cz.tomkren.trhy.helpers;

import cz.tomkren.trhy.Firm;
import cz.tomkren.trhy.Trans;
import cz.tomkren.trhy.Trh;

public class OldConsoleMain {

    public static void main(String[] args) {
        Log.it("MARKETS!\n");

        Trh trh = new Trh();

        try {
            trh.addFirm("Penuel Katz", Firm.Examples.mkKolonialKatz());
            //trh.addFirm("Penuel Katz Fake",Firm.Examples.mkKolonialKatz()); // má vyhodit výjimku že už se tak něco jmenuje
            trh.addFirm("Václav Rolný", Firm.Examples.mkPoleAS());

            Log.it();
            Log.it(trh);
            InventoryDump trhInvDump1 = trh.getInventoryDump();

            trh.send(Trans.mkSlowBuy("Penuel Katz", "Koloniál Katz", "Flour", 1000, 10));
            trh.send(Trans.mkSlowSell("Václav Rolný", "Pole a.s.",     "Flour", 200, 12));
            trh.send(Trans.mkQuickBuy("Penuel Katz",  "Koloniál Katz", "Flour", 24));

            trh.send(Trans.mkSlowSell("Václav Rolný", "Pole a.s.",     "Flour", 250, 15));
            trh.send(Trans.mkQuickBuy("Penuel Katz",  "Koloniál Katz", "Work", 100));
            trh.send(Trans.mkSlowBuy( "Penuel Katz",  "Koloniál Katz", "Flour", 20, 5));


            Log.it();
            Log.it(trh);
            InventoryDump trhInvDump2 = trh.getInventoryDump();
            trhInvDump1.compare(trhInvDump2, false) ;


        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> "+e.getMessage());
        }


        //Log.it( InventoryDump.isAlmostTheSame(2,2.0000000001) );
        //Log.it( InventoryDump.isAlmostTheSame(2,2.000000001) );




    }
}
