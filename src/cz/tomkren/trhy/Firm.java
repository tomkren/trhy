package cz.tomkren.trhy;

import cz.tomkren.observer.BasicChangeInformer;
import cz.tomkren.observer.ChangeInformer;
import cz.tomkren.trhy.stuff.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Tomáš Křen
 */
public class Firm {

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
        ret.addAll( getComos(sgInventory) );
        return ret;
    }

    private List<Commodity> getComos (Map<String,? extends Stuff> inventory) {
        return inventory.values().stream().map(Stuff::getComo).collect(Collectors.toList());
    }

    public boolean hasEnoughMoney (double m) {
        return money >= m; 
    }
    
    public boolean hasEnoughCommodity(String comoName, double num) {
        PluralStuff e = plInventory.get(comoName);
        return e != null && e.getNum() >= num;
    }

    //todo work method
    public double work (String machineID) {

        throw new UnsupportedOperationException();
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

}
