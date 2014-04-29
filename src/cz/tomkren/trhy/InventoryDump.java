package cz.tomkren.trhy;


import java.util.*;

public class InventoryDump {

    private Map<String,Double> dump;

    public static interface Dumpable {
        Map.Entry<String,Double> dump();
    }

    public InventoryDump () {
        dump = new HashMap<String, Double>();
    }

    public InventoryDump (Collection<? extends Dumpable> xs) {
        this();
        for (Dumpable x : xs) {
            Map.Entry<String,Double> e = x.dump();
            String key = e.getKey();
            Double val = e.getValue();
            Double oldVal = dump.get(key);
            if (oldVal == null) {
                dump.put(key, val);
            } else {
                dump.put(key, oldVal+val);
            }
        }
    }

    // todo pokusit se decouplenout
    public InventoryDump (Firm firm) {
        this();

        dump.put("$", firm.getMoney() );

        for (Map.Entry<String, Firm.Elem> entry : firm.getInventoryMap().entrySet()) {
            String key = entry.getKey();
            Firm.Elem elem = entry.getValue();

            if (elem instanceof Firm.NumElem) {
                Firm.NumElem numElem = (Firm.NumElem) elem;
                dump.put(key, numElem.getNum());
            }
        }
    }
    
    public void add( InventoryDump inventoryDump2 ) {
        for (Map.Entry<String,Double> e : inventoryDump2.dump.entrySet()) {
            String key = e.getKey();
            Double val = e.getValue();
            Double oldVal = dump.get(key);
            if (oldVal == null) {
                dump.put(key, val);
            } else {
                dump.put(key, oldVal+val);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (Map.Entry<String,Double> e: dump.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue()).append("; ");
        }
        sb.append(" }");
        return sb.toString();
    }


    public boolean porovnej(Object obj) {
        if (!(obj instanceof InventoryDump)) {
            Log.it("nemečuje typ");
            return false;
        }

        InventoryDump invDump2 = ((InventoryDump) obj);
        
        Map<String,Double> dump2 = invDump2.dump;

        if (dump.size() != dump2.size()) {
            Log.it("nemečuje size");
            return false;
        }

        for (Map.Entry<String,Double> entry: dump.entrySet()) {
            double val1 = entry.getValue();
            double val2 = dump2.get(entry.getKey());
            if ( val1 != val2 ) {
                Log.it("nemečuje množství "+entry.getKey()+" "+val1+" != "+val2);
                return false;
            }
        }

        Log.it("---------------------------");
        Log.it("Hurá, tyto 2 inventory dumps jsou stejný:");
        Log.it(this);
        Log.it(invDump2);
        Log.it("---------------------------");

        return true;
    }
}
