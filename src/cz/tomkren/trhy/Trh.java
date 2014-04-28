package cz.tomkren.trhy;

import java.util.*;

/**
 *
 * @author Tomáš Křen
 */

public class Trh {
    private Map<String,Tabule>   tabs;      // komodita -> tabule tý komodity
    private Map<String,Firm>        firms;     // firmID   -> majetek tý firmy   
    private Map<String,Set<String>> ownership; // agentID  -> mn. firmID co má
                                               // pozdějc bude fikanějc, aby
                                               // šli i jiný vztahy než má
    private int currentTik;   // současnej čas simulace
    private int numTrans;     // aneb nasledující transaction ID
    private List<String> log; // Trhový log.


    public Trh () {
        tabs      = new HashMap<String, Tabule>();
        firms     = new HashMap<String, Firm>();
        ownership = new HashMap<String, Set<String>>();

        numTrans   = 0;
        currentTik = 0;
        log = new LinkedList<String>();
    }


//~~~~ ~~~~~ ~~~~ ~~~~~ ~~~~ ~~ ~~~~~ ~~~~~ ~~~~~~ ~~~~~ ~~~~

    //  TODO výstup metody send je, aby pak agent trhu mohl informovat agenty manažírků.
    public List<Trans.Res> send (Trans.Req req) {

        log(req.toString());

        Trans.CheckStatus checkStatus = checkTransReq(req);

        if (checkStatus.isOk()) {

            try {
                subtractFromFirm(req);
            } catch (TrhException e) {
                log( "[TRANSACTION FAILED | SUBTRACT EXCEPTION]  " + e.getMessage() );
            }

            try {

                List<Trans.Res> transResults = addToTabule(req);
                performResultsUpdate(transResults);
                return transResults;

            } catch (TrhException e) {
                log( "[TRANSACTION FAILED | addToTabule EXCEPTION]  " + e.getMessage() );
                return null;
            }

        } else {
            log("[TRANSACTION FAILED | CHECK]  " + checkStatus.getMsg());
            return null;
        }

    }

    private void performResultsUpdate (List<Trans.Res> rs) {
        for (Trans.Res r : rs) {
            performResultUpdate(r);
        }
    }

    private void performResultUpdate (Trans.Res res) {

        log("  "+res);

        // TODO zpracovat transResults
        //  TODO (1) přičíst peníze/commodity dle výsledků
        // nesou sebou informaci co je do jakého inventáře potřeba přidat

        /* todo
        Tansaction.Result.ResultType resType = ... ;
        boolean isFail = ... ; //due to NO_Q_* mozna staciv resType

        // plan : vypsat to tu jednotlive a pak fikane sloucit
        switch (resType) {      // todo : zkontrolovat ze se to tak chova
            case BUY       : .. // add veci
            case SELL      : .. // add penize
            case NO_Q_BUY  : .. // add penize
            case NO_Q_SELL : .. // add veci
            default: ..
        }
        */


    }



//~~~~ ~~~~~ ~~~~ ~~~~~ ~~~~ ~~ ~~~~~ ~~~~~ ~~~~~~ ~~~~~ ~~~~



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[[TRH DUMP BEGIN]]\n\n");

        for (Map.Entry<String, Set<String>> e : ownership.entrySet()) {
            sb.append(e.getKey()).append(" -> ").append(e.getValue().toString()).append("\n");
        }
        sb.append("\n");

        for (Map.Entry<String, Firm> e : firms.entrySet()) {
            sb.append(e.getValue().toString());
        }

        for (Map.Entry<String, Tabule> e : tabs.entrySet()) {
            sb.append(e.getValue().toString());
        }


        sb.append("[[TRH DUMP END]]\n\n");
        return sb.toString();
    }

    public void log(Object o){
        Log.it("<TRH-LOG>        "+o);
        log.add(o.toString());
    }

    public List<String> getLog() {
        return log;
    }
    
    public void incrementTik () {currentTik ++;}
    public int getTik () {return currentTik;}
    
    private int nextTransID () {
        return numTrans ++;
    }
    
    public void addTabule (Commodity c) {
        tabs.put(c.getName(), new Tabule(c));
    }
    
    public void addAgentsFirm (String agentID, Firm hisFirm) throws TrhException {
        

        String firmID = hisFirm.getFirmID();
        
        if (firms.containsKey(firmID)) {
            throw new TrhException("Firma s takovým názvem (\""+firmID+"\") už na trhu je.");
        }
        
        Set<String> hisFirmIDs = ownership.get(agentID);
        
        if (hisFirmIDs == null) {
            hisFirmIDs = new HashSet<String>();
            ownership.put(agentID, hisFirmIDs);
        }
        
        hisFirmIDs.add(firmID);
        firms.put(firmID, hisFirm);

        // projdi všechny komodity a pokud pro ně ještě neni trh tak ho udělej..
        for (Map.Entry<String, Firm.Elem> entry : hisFirm.getInventoryMap().entrySet()) {
            String como = entry.getKey();
            if (!tabs.containsKey(como)) {
                addTabule(entry.getValue().getCommodity());
            }
        }

        log("ADD FIRM \'"+firmID+"\'");
    }



    public boolean isOwner (String agentID, String firmID) {
        Set<String> hisFirms = ownership.get(agentID);
        return hisFirms != null && hisFirms.contains(firmID);
    }

    public Trans.CheckStatus checkTransReq (Trans.Req req) {

        if (!isOwner(req.getAID(), req.getFID())) {
            return Trans.ko("Agent není majitelem firmy.");
        }

        if (!req.isQuick() && req.getPrice() <= 0) {
            return Trans.ko("Price must be > 0.");
        }

        String comoName = req.getComoName();

        // todo : zvážit zda nenahradit vytvořením té tabule radši
        if (!tabs.containsKey(comoName)){
            return Trans.ko("Tato komodita se na trhu neobchoduje.");
        }

        Firm firm = firms.get(req.getFID());

        if (req instanceof Trans.Buy) {
            Trans.Buy buy = (Trans.Buy) req;

            double money = buy.getMoney();
            if (money <= 0) {return Trans.ko("BUY Money must be > 0.");}

            if (firm.hasEnoughMoney(money)) {
                return Trans.OK;
            } else {
                return Trans.ko("Firma nemá požadované množství peněz.");
            }
        }

        if (req instanceof Trans.Sell) {
            Trans.Sell sell = (Trans.Sell) req;

            double num = sell.getNum();
            if (num <= 0) {return Trans.ko("SELL Num must be > 0.");}

            if (firm.hasEnoughComodity(comoName,num)) {
                return Trans.OK;
            } else {
                return Trans.ko("Firma nemá požadované množství komodity.");
            }
        }

        return Trans.ko("Unsupported transaction request format.");
    }



    private void subtractFromFirm(Trans.Req req) throws TrhException {

        Firm firm = firms.get(req.getFID()); // opakuje se zbytečně (při čekách už znám)

        if (req instanceof Trans.Sell) {
            Trans.Sell sell = (Trans.Sell) req;
            Commodity commodity = req.getComo();

            double newNum = firm.addComodity(commodity, -sell.getNum() );

            if (newNum < 0) {
                firm.addComodity(commodity, sell.getNum() );
                throw new TrhException("Komodita " + commodity + " ve firme " +
                        req.getFID() + " se dostala pod nulu, operace byla zvracena."+
                        "Bylo by tam mnozstvi " + newNum + ".");
            }


        } else if (req instanceof Trans.Buy) {
            Trans.Buy buy = (Trans.Buy) req;

            double newMoney = firm.addMoney(-buy.getMoney());

            if (newMoney < 0) {
                firm.addMoney( buy.getMoney() );
                throw new TrhException("Peníze ve firme " +
                        req.getFID() + " se dostali pod nulu, operace byla zvracena."+
                        "Bylo by tam $" + newMoney + ".");
            }
        }


    }

    private List<Trans.Res> addToTabule(Trans.Req req) throws TrhException {
        Tabule tab = tabs.get(req.getComoName());
        if (tab == null) { throw new TrhException("Požadovaná tabule není na trhu."); }

        return tab.add(req, nextTransID(), currentTik);
    }
    
    public static class TrhException extends Exception {

        private String msg;
        
        public TrhException(String msg) {
            this.msg = msg;
        }
        
        @Override
        public String getMessage() {
            return "[TRH EXCEPTION] : "+msg;
        }
        
    }
    
    
}
