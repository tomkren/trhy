package cz.tomkren.trhy;

import cz.tomkren.fishtron.Type;
import cz.tomkren.observer.BasicChangeInformer;
import cz.tomkren.observer.ChangeInformer;
import cz.tomkren.trhy.helpers.InventoryDump;
import cz.tomkren.trhy.stuff.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        public enum Status {OK, KO_NOT_ENOUGH_INPUT, KO_WRONG_ID, KO_NOT_MACHINE, KO_CHECK_INPUT_FAIL}

        private Status status;
        private Stuff  output;

        public WorkRes(Status status, Stuff output) {
            this.status = status;
            this.output = output;
        }
        public Status getStatus() {
            return status;
        }
        public Stuff  getOutput() {
            return output;
        }

        public static WorkRes ok(Stuff output) { return new WorkRes(Status.OK                  , output ); }
        public static WorkRes wrongID()        { return new WorkRes(Status.KO_WRONG_ID         , null   ); }
        public static WorkRes notMachine()     { return new WorkRes(Status.KO_NOT_MACHINE      , null   ); }
        public static WorkRes checkInputFail() { return new WorkRes(Status.KO_CHECK_INPUT_FAIL , null   ); }
        public static WorkRes notEnoughInput() { return new WorkRes(Status.KO_NOT_ENOUGH_INPUT , null   ); }


    }

    public WorkRes doWork (String machineID, Stuff input) {

        // kontroly vstupu stroje 1 (mám dost suroviny?)
        if (!hasEnoughStuff(input)) { return WorkRes.notEnoughInput(); }

        // kontroly stroje
        SingularStuff sgStuff = sgInventory.get(machineID);

        if (sgStuff == null)               { return WorkRes.wrongID();    }
        if (!(sgStuff instanceof Machine)) { return WorkRes.notMachine(); }

        Machine machine = (Machine) sgStuff;

        // kontroly vstupu stroje 2 (pasuje surovina do stroje?)
        if (!machine.checkInput(input)) { return WorkRes.checkInputFail(); }

        Stuff output = machine.work(input);

        // odebrat použité vstupy a přičíst vzniklé výstupy
        subtractCommodity(input);
        addCommodity(output);

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

    public List<Type> getComos () {
        List<Type> ret;
        ret =       getComos(plInventory)  ;
        ret.addAll(getComos(sgInventory));
        return ret;
    }

    private List<Type> getComos (Map<String,? extends Stuff> inventory) {
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

    public double addCommodity (Stuff s) {
        if (s instanceof PluralStuff){
            PluralStuff ps = (PluralStuff) s;
            return addCommodity(ps.getComo(), ps.getNum());
        }
        // todo pro SingularStuff
        throw new UnsupportedOperationException("todo : addCommodity(Stuff s) pro SingularStuff");
    }

    public double subtractCommodity (Stuff s) {
        if (s instanceof PluralStuff) {
            PluralStuff ps = (PluralStuff) s;
            return addCommodity(ps.getComo(),-ps.getNum());
        }
        // todo pro SingularStuff
        throw new UnsupportedOperationException("todo : subtractCommodity(Stuff s) pro SingularStuff");
    }

    // todo nápodobně odpublicovat
    public double addCommodity (Type c, double delta) {
        double ret; // kolik je komodity po přidání
        PluralStuff e = plInventory.get(c.toString());
        if (e == null) {
            plInventory.put(c.toString(), new Quantum(c, delta));
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
