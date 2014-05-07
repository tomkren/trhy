package cz.tomkren.trhy;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class TrhTester {

    private Trh trh;
    private Random rand;


    public TrhTester (Trh t, Long seed) {
        trh  = t;
        rand = seed == null ? new Random() : new Random(seed);
    }

    public TrhTester (Trh t) {
        this(t, null);
    }

    public boolean sendRandomTrans() {

        Log.it("Sending random transaction..");

        InventoryDump beforeDump = trh.getInventoryDump();

        // select random agent id
        List<String> AIDs = trh.getAIDs();
        String aid = AIDs.get(rand.nextInt(AIDs.size()));

        Log.it("agent: "+ aid);

        // select one of his firms
        Set<String> fidsSet = trh.getFIDsForAID(aid);
        String[] fids = fidsSet.toArray(new String[fidsSet.size()]);
        String fid = fids[rand.nextInt(fids.length)];

        Firm firm = trh.getFirm(fid);
        InventoryDump firmDump = firm.getInventoryDump();

        Log.it("firm: "+ fid + " ... " +firmDump);

        // select commodity
        List<String> comoNames = firmDump.getComoNames(false);
        String comoName = comoNames.get(rand.nextInt(comoNames.size()));

        Log.it("como: "+ comoName);

        boolean isBuy   = rand.nextBoolean();
        boolean isQuick = rand.nextBoolean();

        Trans.Req req;
        
        if (isBuy) {
            // select money to buy
            double firmMoney   = firm.getMoney();
            double percent     = randDouble(0, 0.2);
            double moneyForBuy = firmMoney * percent;
            
            if (isQuick) {
                req = Trans.mkQuickBuy(aid, fid, comoName, moneyForBuy); 
            } else {
                // todo price
                double price = 7777;
                req = Trans.mkSlowBuy(aid, fid, comoName, moneyForBuy, price);
            }
        } else { // sell
            // select num to sell
            double comoNum    = firm.getComoNum(comoName);
            double percent    = randDouble(0, 0.2);
            double numForSell = comoNum * percent;

            if (isQuick) {
                req = Trans.mkQuickSell(aid, fid, comoName, numForSell);
            } else {
                // todo price
                double price = 8888;
                req = Trans.mkSlowSell(aid, fid, comoName, numForSell, price);
            }
        }

        Log.it("req: " + req);

        trh.send(req);

        InventoryDump afterDump = trh.getInventoryDump();
        return beforeDump.porovnej(afterDump, false);
    }

    public static final double P_OF_DUMP_PRICE = 0.1;


    //TODO rozdelaný dodělat ....

    private double getRandPrice(String comoName, boolean isBuy) {
        PriceInfo pi = trh.getTabule(comoName).getPriceInfo();

        if (pi.isEmpty()) {
            return randDouble(1,100);
        }

        if (rand.nextDouble() < P_OF_DUMP_PRICE) {
            return randDouble(pi.getLow(), pi.getHigh());
        }

        //TODO !!!!!!!!!!

        if (isBuy) {
            if (pi.isNothingToBuy()) {

            } else {


            }
        } else {

        }

        throw new UnsupportedOperationException();
    }

    private double randDouble (double from, double to) {
        return from + (to-from) * rand.nextDouble();
    }

}
