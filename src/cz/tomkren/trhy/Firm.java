package cz.tomkren.trhy;

import cz.tomkren.observer.BasicChangeInformer;
import cz.tomkren.observer.ChangeInformer;
import cz.tomkren.trhy.stuff.Commodity;
import cz.tomkren.trhy.helpers.InventoryDump;
import cz.tomkren.trhy.stuff.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Tomáš Křen
 */
public class Firm {

    private String firmID;

    private double money;
    private Map<String,PluralStuff>   plInventory;
    private Map<String,SingularStuff> sgInventory;

    private BasicChangeInformer changeInformer;

    public Firm (String firmID) {
        this.firmID = firmID;

        money       = 0;
        plInventory = new HashMap<>();
        sgInventory = new HashMap<>();

        changeInformer = new BasicChangeInformer();
    }

    public Firm (String firmID, Stuff[] stuffs) {
        this(firmID);
        for (Stuff s : stuffs) {
            if (s instanceof Money) {
                money += ((Money)s).getNum();
            } else if (s instanceof PluralStuff) {
                PluralStuff ps = (PluralStuff) s;
                plInventory.put(ps.getComoName(), ps);
            } else if (s instanceof SingularStuff) {
                SingularStuff ss = (SingularStuff) s;
                sgInventory.put(ss.getSgID(), ss);
            }
        }
    }

    public static class WorkRes {
        public enum Status {OK, KO_WRONG_ID, KO_NOT_MACHINE, KO_CHECK_INPUT_FAIL}

        private Status status;
        private Stuff  output;

        public WorkRes(Status status, Stuff output) {
            this.status = status;
            this.output = output;
        }

        public static WorkRes ok(Stuff output) { return new WorkRes(Status.OK                  , output ); }
        public static WorkRes wrongID()        { return new WorkRes(Status.KO_WRONG_ID         , null   ); }
        public static WorkRes notMachine()     { return new WorkRes(Status.KO_NOT_MACHINE      , null   ); }
        public static WorkRes checkInputFail() { return new WorkRes(Status.KO_CHECK_INPUT_FAIL , null   ); }

    }




    // todo : teď dělaný jakoby se output dodával zvenčí a zas se posílal ven, ale on se bere z firmy a taky se tam vrací...
    public WorkRes doWork (String machineID, Stuff input) {

        // kontroly vstupu stroje (mám dost suroviny?)


        // kontroly stroje
        SingularStuff sgStuff = sgInventory.get(machineID);

        if (sgStuff == null)               { return WorkRes.wrongID();    }
        if (!(sgStuff instanceof Machine)) { return WorkRes.notMachine(); }

        Machine machine = (Machine) sgStuff;

        if (!machine.checkInput(input))    { return WorkRes.checkInputFail(); }

        Stuff output = machine.work(input);

        return WorkRes.ok(output);
    }

    public ChangeInformer getChangeInformer() {
        return changeInformer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== "+firmID+" ===\n\n");

        for (SingularStuff s : sgInventory.values()) {
            sb.append( s.getSgID() ).append(" : ").append( s.getComoName() ).append("\n");
        }

        sb.append("\n");
        sb.append("$ ... ").append(money).append("\n");

        for (Map.Entry<String, PluralStuff> e : plInventory.entrySet()) {
            sb.append( e.getKey() ).append(" ... ").append( e.getValue().getNum() ).append("\n");
        }

        sb.append("\n");

        return sb.toString();
    }


    public InventoryDump getInventoryDump() {
        InventoryDump dump;
        dump =    new InventoryDump(plInventory.values())  ;
        dump.add( new InventoryDump(sgInventory.values()) );
        dump.add( new InventoryDump("$", money) );
        return dump;
    }

    public List<Commodity> getComos () {
        List<Commodity> ret;
        ret =       getComos(plInventory)  ;
        ret.addAll(getComos(sgInventory));
        return ret;
    }

    private List<Commodity> getComos (Map<String,? extends Stuff> inventory) {
        return inventory.values().stream().map(Stuff::getComo).collect(Collectors.toList());
    }

    public boolean hasEnoughStuff (Stuff stuff) {
        if (stuff instanceof Money) {
            return hasEnoughMoney(((Money) stuff).getNum());
        } else if (stuff instanceof PluralStuff) {
            PluralStuff ps = (PluralStuff) stuff;
            return hasEnoughCommodity(ps.getComoName(), ps.getNum());
        } else if (stuff instanceof SingularStuff) {
            return hasEnoughSg((SingularStuff) stuff);
        }
        return false;
    }

    // todo | nahradit všude hasEnoughStuff(Stuff stuff) variantou
    // todo | (pomocí že to dám nejdřív private, a kde to hodí chyby tak tam se to změní)
    // todo | udělat to i u ostatních metodo co jsou použitý v hasEnoughStuff(Stuff stuff)
    public boolean hasEnoughCommodity (String comoName, double num) {
        PluralStuff e = plInventory.get(comoName);
        return e != null && e.getNum() >= num;
    }

    // todo viz hasEnoughCommodity
    public boolean hasEnoughMoney (double m) {
        return money >= m;
    }

    // todo viz hasEnoughCommodity
    private boolean hasEnoughSg(SingularStuff stuff) {
        return sgInventory.get(stuff.getSgID()) != null;
    }



    public double addMoney (double delta) {
        money += delta;
        changeInformer.informListeners();
        return money;
    }
    
    public double addCommodity (Commodity c, double delta) {
        double ret; // kolik je komodity po přidání
        PluralStuff e = plInventory.get(c.getName());
        if (e == null) {
            plInventory.put(c.getName(), new Quantum(c, delta));
            ret = delta;
        } else {
            ret = e.addNum(delta);
        }
        changeInformer.informListeners();
        return ret;
    }
    
    public String getFirmID () {
        return firmID;
    }


    public double getMoney() {
        return money;
    }

    public double getComoNum (String comoName) {
        PluralStuff e = plInventory.get(comoName);
        if (e == null) {return 0;}
        return e.getNum();
    }


    /* --- EXAMPLE FIRMS ------------------------------------------------------------------------------------------- */

    public static class Examples {
        public static Firm mkKolonialKatz() {
            return new Firm(
                    "Koloniál Katz", //Penuel Katz
                    new Stuff[]{
                            Stuff.money(100000),
                            Stuff.quantum("Work", 5),
                            Stuff.quantum("Flour", 5000),
                            Stuff.quantum("Pie", 100)
                    }
            );
        }

        public static Firm mkPoleAS() {
            return new Firm(
                    "Pole a.s.",
                    new Stuff[]{
                            Stuff.money(1000),
                            Stuff.quantum("Work", 1000),
                            Stuff.quantum("Flour", 5000),
                            Stuff.simpleMachine("pole", "Work", "Flour", 2)
                    }
            );
        }
    }

}
