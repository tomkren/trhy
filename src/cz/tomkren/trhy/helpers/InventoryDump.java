package cz.tomkren.trhy.helpers;
import cz.tomkren.trhy.stuff.SimpleMachine;
import cz.tomkren.trhy.stuff.Stuff;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// todo neřeší singular stuff
public class InventoryDump {

    private Map<String,Double> dump;

    public double get(String key) {
        Double val = dump.get(key);
        if (val == null) {return 0;}
        return val;
    }

    public InventoryDump () {
        dump = new HashMap<>();
    }

    private void updateDump(String key, Double newVal) {
        Double oldVal = dump.get(key);
        if (oldVal == null) {
            dump.put(key, newVal);
        } else {
            dump.put(key, oldVal+newVal);
        }
    }

    public InventoryDump (String key, double val) {
        this();
        updateDump(key, val);
    }

    public InventoryDump (Collection<? extends Stuff> xs) {
        this();
        for (Stuff x : xs) {
            updateDump(x.dumpKey(), x.dumpVal());
        }
    }

    public InventoryDump (Map<String,Double> dumpMap) {
        this();
        for (Map.Entry<String,Double> e : dumpMap.entrySet()) {
            updateDump(e.getKey(), e.getValue());
        }
    }

    public List<String> getComoNames(boolean includeMoney, boolean includeMachines) {
        return dump.keySet().stream()
                .filter(cName -> (includeMoney || !cName.equals("$")) && (includeMachines || !Utils.isMachineName(cName)))
                .collect(Collectors.toList());
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


    // todo otestovat a použít někde
    public boolean compareWorkChange (SimpleMachine m, InventoryDump after) {

        String aComoName  = m.getType().getInput().toString();
        String bComoName = m.getType().getOutput().toString();

        double aBefore = get(aComoName);
        double bBefore = get(bComoName);
        double aAfter  = after.get(aComoName);
        double bAfter  = after.get(bComoName);

        double aDelta = aBefore - aAfter;
        double bDelta = bAfter  - bBefore;

        return Utils.isAlmostTheSame( m.getBeta() , bDelta/aDelta );
    }

    // todo odebrat ty logy a dát je na místa kde se používá tahle metoda
    public boolean compare(InventoryDump invDump2, boolean isSilent) {

        Map<String,Double> dump2 = invDump2.dump;

        if (dump.size() != dump2.size()) {
            Log.it("nemečuje size", isSilent);
            return false;
        }

        for (Map.Entry<String,Double> entry: dump.entrySet()) {
            double val1 = entry.getValue();
            double val2 = dump2.get(entry.getKey());
            if ( !Utils.isAlmostTheSame(val1, val2) ) {
                Log.it("nemečuje množství "+entry.getKey()+" "+val1+" != "+val2, isSilent);
                return false;
            }
        }

        Log.it("---------------------------", isSilent);
        Log.it("Hurá, tyto 2 inventory dumps jsou stejný:", isSilent);
        Log.it(this, isSilent);
        Log.it(invDump2, isSilent);
        Log.it("---------------------------", isSilent);

        return true;
    }
}
