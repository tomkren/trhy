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
        Log.it("[TRH LOG]: "+o);
        log.add(o.toString());
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

    public void send (Transaction.Request tr) {

        log(tr.toString());

        Transaction.CheckResult checkResult = checkTransactionRequest(tr);

        if (checkResult.isOk()) {

            try {
                subtractFromFirm(tr);
            } catch (TrhException e) {
                log( "[TRANSACTION FAILED | SUBTRACT EXCEPTION]  " + e.getMessage() );
            }

            try {

                List<Transaction.Result> transResults = addToTabule(tr);

                // TODO zpracovat transResults
                //  TODO (1) přičíst peníze/commodity dle výsledků
                //  TODO (2) vrátit i jako výstup metody send, aby pak agent trhu mohl informovat agenty manažírků.
                // nesou sebou informaci co je do jakého inventáře potřeba přidat

                // zatim jen vypíšem:
                log("TODO opravdu tyto výsledky zpracovat, zatím jen vypíšem:");
                for (Transaction.Result res : transResults) {
                    log(res);
                }

            } catch (TrhException e) {
                log( "[TRANSACTION FAILED | addToTabule EXCEPTION]  " + e.getMessage() );
            }

        } else {
            log( "[TRANSACTION FAILED | CHECK]  " + checkResult.getMsg() );
        }

    }

    public boolean isOwner (String agentID, String firmID) {
        Set<String> hisFirms = ownership.get(agentID);
        if (hisFirms == null) {return false;}
        return hisFirms.contains(firmID);
    }
    
    public Transaction.CheckResult checkTransactionRequest (Transaction.Request tr) {
        
        Transaction.Head head = tr.getHead();
        
        if (!isOwner(head.agentID, head.firmID)) {
            return Transaction.ko("Agent není majitelem firmy.");
        }
        
        // todo : zvážit zda má opravdu smysl tohle zakazovat...
        if (tr instanceof Transaction.Slow) {
            Transaction.Slow slow = (Transaction.Slow) tr;
            if (slow.getPrice() <= 0) {return Transaction.ko("Price must be > 0.");}
        }
        
        Commodity commodity = head.commodity;
        
        // todo : zvážit zda nenahradit vytvořením té tabule radši
        if (!tabs.containsKey(commodity.getName())){
            return Transaction.ko("Tato komodita se na trhu neobchoduje.");
        }

        Firm firm = firms.get(head.firmID);
        
        if (tr instanceof Transaction.Buy) {
            Transaction.Buy buy = (Transaction.Buy) tr;
            
            double money = buy.getMoney();
            if (money <= 0) {return Transaction.ko("Money must be > 0.");}
            
            if (firm.hasEnoughMoney(money)) {
                return Transaction.OK;
            } else {
                return Transaction.ko("Firma nemá požadované množství peněz.");
            }
        }
        
        if (tr instanceof Transaction.Sell) {
            Transaction.Sell sell = (Transaction.Sell) tr;

            double num = sell.getNum();
            if (num <= 0) {return Transaction.ko("Num must be > 0.");}
            
            if (firm.hasEnoughComodity(commodity.getName(),num)) {
                return Transaction.OK;
            } else {
                return Transaction.ko("Firma nemá požadované množství komodity.");
            }
        }
        
        return Transaction.ko("Unsupported transaction request format.");
    }
    
    private void subtractFromFirm(Transaction.Request tr) throws TrhException {
        
        Transaction.Head head = tr.getHead();
        Commodity commodity = head.commodity;
        Firm firm = firms.get(head.firmID); // opakuje se zbytečně (při čekách už znám) 
                                            // ale zatim na to srát
        
        if (tr instanceof Transaction.Sell) {
            Transaction.Sell sell = (Transaction.Sell) tr;

            double newNum = firm.addComodity(commodity, -sell.getNum() );
            
            if (newNum < 0) { 
                firm.addComodity(commodity, sell.getNum() );
                throw new TrhException("Komodita " + commodity + " ve firme " +
                    head.firmID + " se dostala pod nulu, operace byla zvracena."+
                    "Bylo by tam mnozstvi " + newNum + ".");
            }
            
            
        } else if (tr instanceof Transaction.Buy) {
            Transaction.Buy buy = (Transaction.Buy) tr;
            
            double newMoney = firm.addMoney(-buy.getMoney());
            
            if (newMoney < 0) {
                firm.addMoney( buy.getMoney() );
                throw new TrhException("Peníze ve firme " +
                    head.firmID + " se dostali pod nulu, operace byla zvracena."+
                    "Bylo by tam $" + newMoney + ".");                
            }
        }
        
        
    }
    
    private List<Transaction.Result> addToTabule (Transaction.Request tr) throws TrhException {

        Tabule tab = tabs.get(tr.getHead().getComo().getName());
        
        if (tab == null) {
            throw new TrhException("Požadovaná tabule není na trhu.");
        }

        return tab.add(tr, nextTransID(), currentTik);

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
