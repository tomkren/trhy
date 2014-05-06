package cz.tomkren.trhy;

public class Main {

    public static void main(String[] args) {
        Log.it("MARKETS!\n");

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



        Trh trh = new Trh();

        try {

            trh.addFirm("Penuel Katz", kolonial);
            //trh.addFirm("Penuel Katz Fake",kolonial); // má vyhodit výjimku že už se tak něco jmenuje
            trh.addFirm("Václav Rolný", poleAS);



            Log.it().it(trh);
            InventoryDump trhInvDump1 = trh.getInventoryDump();

            trh.send(Trans.mkSlowBuy( "Penuel Katz",  "Koloniál Katz", "Flour", 1000, 10));
            trh.send(Trans.mkSlowSell("Václav Rolný", "Pole a.s.",     "Flour", 200, 12));
            trh.send(Trans.mkQuickBuy("Penuel Katz",  "Koloniál Katz", "Flour", 24));

            trh.send(Trans.mkSlowSell("Václav Rolný", "Pole a.s.",     "Flour", 250, 15));
            trh.send(Trans.mkQuickBuy("Penuel Katz",  "Koloniál Katz", "Work", 100));
            trh.send(Trans.mkSlowBuy( "Penuel Katz",  "Koloniál Katz", "Flour", 20, 5));



            Log.it().it(trh);
            InventoryDump trhInvDump2 = trh.getInventoryDump();

            trhInvDump1.porovnej(trhInvDump2, false) ;





        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> "+e.getMessage());
        }






    }
}
