package cz.tomkren.trhy;


import java.util.*;
import java.util.stream.Collectors;

public class InventoryDump {

    private Map<String,Double> dump;

    public static interface Dumpable {
        Map.Entry<String,Double> dump();
    }

    public InventoryDump () {
        dump = new HashMap<>();
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

    public List<String> getComoNames(boolean includeMoney) {
        return dump.keySet().stream()
                            .filter(cName -> includeMoney || !cName.equals("$"))
                            .collect(Collectors.toList());
    }

    // todo pokusit se o decouple
    public InventoryDump (Firm firm) {
        this();

        dump.put("$", firm.getMoney() );

        for (Map.Entry<String, Stuff> entry : firm.getInventoryMap().entrySet()) {
            dump.put(entry.getKey(), entry.getValue().getNum());
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



    public boolean porovnej(InventoryDump invDump2, boolean isSilent) {

        Map<String,Double> dump2 = invDump2.dump;

        if (dump.size() != dump2.size()) {
            Log.it("nemečuje size", isSilent);
            return false;
        }

        for (Map.Entry<String,Double> entry: dump.entrySet()) {
            double val1 = entry.getValue();
            double val2 = dump2.get(entry.getKey());
            if ( !MyUtils.isAlmostTheSame(val1, val2) ) {
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
