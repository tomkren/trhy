package cz.tomkren.trhy;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomáš Křen
 */
public class Firm {
    
    private String firmID;
    private Map<Commodity,Elem> inventory;
    private double money; // protože se do nich šaha opravdu často,
                          // nedáme je pro efektivitu do mapy 
    
    public Firm (String firmID) {
        this.firmID = firmID;
        inventory = new HashMap<Commodity, Elem>();
        money = 0;
    }
    
    public boolean hasEnoughMoney (double m) {
        return money >= m; 
    }
    
    public boolean hasEnoughComodity (Commodity c, double num) {
        Elem e = inventory.get(c);
        if (e == null) {return false;}
        return e.getNum() >= num;
    }
    
    public double addMoney (double delta) {
        money += delta;
        return money;
    }
    
    public double addComodity (Commodity c, double delta) {
        Elem e = inventory.get(c);
        if (e == null) {
            inventory.put(c, new NumElem(delta));
            return delta;
        }
        return e.addNum(delta);
    }
    
    public String getFirmID () {
        return firmID;
    }
    
    public Map<Commodity,Elem> getInventoryMap () {
        return inventory;
    }
    
    public static interface Elem {
        public double getNum();
        public double addNum(double delta);
    }
    
    public static class NumElem implements Elem {
        double num;
        
        public NumElem (double d) {
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
