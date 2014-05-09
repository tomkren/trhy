package cz.tomkren.trhy;

import cz.tomkren.observer.ChangeInformer;
import cz.tomkren.observer.ChangeInformerService;

import java.util.*;

/**
 *
 * @author Tomáš Křen
 */
public class Firm {

    public static class Examples {
        public static Firm mkKolonialKatz() {
            return new Firm(
                    "Koloniál Katz", //Penuel Katz
                    new Item[]{
                            Item.basic("$",100000),
                            Item.basic("Work",5),
                            Item.basic("Flour",5000),
                            Item.basic("Pie",100)
                    }
            );
        }

        public static Firm mkPoleAS() {
            return new Firm(
                    "Pole a.s.",
                    new Item[]{
                            Item.basic("$",1000),
                            Item.basic("Work",1000),
                            Item.basic("Flour",5000),
                            Item.machine("Work", "Flour", 2)
                    }
            );
        }
    }

    private String firmID;
    private Map<String,Stuff> inventory;

    private double money; // protože se do nich šahá opravdu často,
                          // nedáme je pro efektivitu do mapy 

    private ChangeInformer changeInformer;

    public Firm (String firmID) {
        this.firmID = firmID;
        inventory = new HashMap<>();
        money = 0;
        changeInformer = new ChangeInformer();
    }

    public Firm (String firmID, Item[] items) {
        this(firmID);
        for (Item it : items) {
            if (it.getCommodity().toString().equals("$")) {
                money += it.getNum();
            } else {
                inventory.put(it.getCommodity().getName(),new Stuff.Basic(it.getCommodity(),it.getNum()));
            }
        }
    }

    public ChangeInformerService getChangeInformer() {
        return changeInformer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== "+firmID+" ===\n");
        sb.append("$ ... ").append(money).append("\n");

        for (Map.Entry<String, Stuff> entry : inventory.entrySet()) {
            String key = entry.getKey();
            Stuff value = entry.getValue();

            sb.append(key).append(" ... ").append(value.getNum()).append("\n");
        }

        //sb.append("... ").append( getInventoryDump() ).append("\n");

        return sb.append("\n").toString();
    }

    public InventoryDump getInventoryDump() {
        return new InventoryDump(this);
    }

    public boolean hasEnoughMoney (double m) {
        return money >= m; 
    }
    
    public boolean hasEnoughCommodity(String comoName, double num) {
        Stuff e = inventory.get(comoName);
        return e != null && e.getNum() >= num;
    }
    
    public double addMoney (double delta) {
        money += delta;
        changeInformer.informListeners();
        return money;
    }
    
    public double addCommodity (Commodity c, double delta) {
        double ret; // kolik je komodity po přidání
        Stuff e = inventory.get(c.getName());
        if (e == null) {
            inventory.put(c.getName(), new Stuff.Basic(c,delta));
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
    
    public Map<String,Stuff> getInventoryMap () {
        return inventory;
    }

    public double getMoney() {
        return money;
    }

    public double getComoNum (String comoName) {
        Stuff e = inventory.get(comoName);
        if (e == null) {return 0;}
        return e.getNum();
    }

}
