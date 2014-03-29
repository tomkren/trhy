package cz.tomkren.trhy;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomáš Křen
 */
public class Firm {
    
    private String firmID;
    private Map<String,Elem> inventory;
    private double money; // protože se do nich šaha opravdu často,
                          // nedáme je pro efektivitu do mapy 
    
    public Firm (String firmID) {
        this.firmID = firmID;
        inventory = new HashMap<String, Elem>();
        money = 0;
    }

    public Firm (String firmID, Item[] items) {
        this(firmID);
        for (Item it : items) {
            if (it.getCommodity().toString().equals("$")) {
                money += it.getNum();
            } else {
                inventory.put(it.getCommodity().getName(),new NumElem(it.getCommodity(),it.getNum()));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== "+firmID+" ===\n");
        sb.append("$ ... "+money+"\n");

        for (Map.Entry<String, Elem> entry : inventory.entrySet()) {
            String key = entry.getKey();
            Elem value = entry.getValue();

            sb.append(key+ " ... "+value.getNum() + "\n");
        }

        return sb.append("\n").toString();
    }

    public boolean hasEnoughMoney (double m) {
        return money >= m; 
    }
    
    public boolean hasEnoughComodity (String comoName, double num) {
        Elem e = inventory.get(comoName);
        if (e == null) {return false;}
        return e.getNum() >= num;
    }
    
    public double addMoney (double delta) {
        money += delta;
        return money;
    }
    
    public double addComodity (Commodity c, double delta) {
        Elem e = inventory.get(c.getName());
        if (e == null) {
            inventory.put(c.getName(), new NumElem(c,delta));
            return delta;
        }
        return e.addNum(delta);
    }
    
    public String getFirmID () {
        return firmID;
    }
    
    public Map<String,Elem> getInventoryMap () {
        return inventory;
    }
    
    public static interface Elem {
        public Commodity getCommodity();
        public double getNum();
        public double addNum(double delta);

    }
    
    public static class NumElem implements Elem {
        double num;
        Commodity como;

        public Commodity getCommodity() {
            return como;
        }

        public NumElem (Commodity c, double d) {
            como = c;
            num = d;
        }
        
        public double getNum() {
            return num;
        } 
        
        public double addNum(double delta){
            num += delta;
            return num;
        } 
    }

}
