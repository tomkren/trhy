package cz.tomkren.views;
import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.*;

import javax.swing.*;
import java.awt.*;

public class TrhView implements ChangeListener {

    public static void main(String[] args) {

        ViewUtils.setLookAndFeel();

        Trh trh = new Trh();
        new TrhView(trh);
        trh.setIsSilent(true);

        try {
            trh.addFirm("Penuel Katz" , Firm.Examples.mkKolonialKatz());
            trh.addFirm("Václav Rolný", Firm.Examples.mkPoleAS());
        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> "+e.getMessage());
        }

    }

    public static final Point FRAME_POS = new Point(0,0);
    public static final int   WIDTH_HAX = 950;

    private Trh trh;
    private boolean autoUpdate;

    private JLabel tikLabel;
    private JPanel panel;

    private JComboBox<String> firmComboBox;
    private JComboBox<String> comoComboBox;
    private JButton           showFirmButton;
    private JButton           showTabuleButton;
    private JButton           showAllFirmsButton;
    private JButton           showAllTablesButton;

    private JTextArea logTextArea;

    private JComboBox<String> transFidComboBox;
    private JComboBox<String> transComoComboBox;
    private JComboBox<String> transAidComboBox;
    private JComboBox         buyOrSellComboBox;
    private JComboBox         quickOrSlowComboBox;
    private JTextField        priceTextField;
    private JTextField        transRestTextField;
    private JButton           sendTransactionButton;
    private JLabel            transRestLabel;
    private JButton           sendRandTransButton;
    private JSpinner          nSpinner;
    private JCheckBox         includeMachinesCheckBox;
    private JButton showAllAllButton;
    private JButton EXITButton;


    public TrhView(Trh t) {
        trh = t;
        autoUpdate = true;
        ViewUtils.mkFrameAndRegister("Market", panel, trh.getChangeInformer(), this, FRAME_POS);

        draw();

        final TrhTester trhTester = new TrhTester(t);
        sendRandTransButton.addActionListener(e -> sendRandTrans(trhTester));
        nSpinner.setValue(1000);

        EXITButton.addActionListener(e -> System.exit(0));

        showFirmButton       .addActionListener(e -> showFirm());
        showTabuleButton     .addActionListener(e -> showTabule());

        showAllFirmsButton   .addActionListener(e -> showAllFirms());
        showAllTablesButton  .addActionListener(e -> showAllTables());
        showAllAllButton     .addActionListener(e -> {showAllFirms();showAllTables();});

        buyOrSellComboBox    .addActionListener(e -> buyOrSell() );
        sendTransactionButton.addActionListener(e -> sendTransaction() );
        quickOrSlowComboBox  .addActionListener(e -> quickOrSlow() );
    }

    private void sendRandTrans(TrhTester trhTester) {
        int n = (Integer) nSpinner.getValue();
        boolean includeMachines = includeMachinesCheckBox.isSelected();

        //Log.it("Sending "+ n +" random transactions, yeehaa!");

        doWithoutRedrawsThenRedraw(()->{
            boolean testResult = trhTester.sendRandomTrans(n,includeMachines);
            Log.it("Send " + n + " rand trans test result: " + (testResult ? "OK" : "KO!!!!!!!!!!!!!!!!"));
        });
    }

    private void doWithoutRedrawsThenRedraw (Utils.VoidAction a) {
        setAutoUpdate(false);
        a.doIt();
        setAutoUpdate(true);
        draw();
    }

    @Override
    public void onChange() {
        if (autoUpdate) {
            draw();
        }
    }

    private void draw() {
        tikLabel.setText( Integer.toString(trh.getTik()) );

        String[] aids, fids, tabs;
        aids = trh.getAIDsArray();
        fids = trh.getFIDsArray();
        tabs = trh.getTabsArray();

        loadComboBox(transAidComboBox  , aids );
        loadComboBox(transFidComboBox, fids);
        loadComboBox(firmComboBox      , fids );
        loadComboBox(comoComboBox, tabs);
        loadComboBox(transComoComboBox , tabs );

        StringBuilder sb = new StringBuilder();
        for (final String str : trh.getLog()) {
            sb.append(str).append("\n");
        }
        logTextArea.setText(sb.toString());
    }

    private static void loadComboBox (JComboBox<String> comboBox, String[] strings) {
        ComboBoxModel<String> model = new DefaultComboBoxModel<>( strings );
        comboBox.setModel(model);
    }

    private void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }



    // ----  button metody -----------------------------------------------------------------------------------------------

    private void quickOrSlow() {
        boolean isQuick = "QUICK".equals(quickOrSlowComboBox.getSelectedItem());
        priceTextField.setEnabled(!isQuick);
    }

    private void buyOrSell() {
        boolean isBuy = "BUY".equals(buyOrSellComboBox.getSelectedItem());
        transRestLabel.setText( isBuy ? "#$:" : "#:" );
    }

    private void sendTransaction() {
        String aid = (String) transAidComboBox.getSelectedItem();
        String fid = (String) transFidComboBox.getSelectedItem();
        String comoName = (String) transComoComboBox.getSelectedItem();
        boolean isBuy = "BUY".equals(buyOrSellComboBox.getSelectedItem());
        boolean isQuick = "QUICK".equals(quickOrSlowComboBox.getSelectedItem());
        double rest, price;
        try {
            rest = Double.parseDouble(transRestTextField.getText());
        } catch (NumberFormatException ex) {
            rest = 0;
        }
        try {
            price = Double.parseDouble(priceTextField.getText());
        } catch (NumberFormatException ex) {
            price = 0;
        }

        Log.it("<" + aid + "> via <" + fid + "> " + (isQuick ? "QUICK" : "SLOW") + " " + (isBuy ? "BUY" : "SELL") +
                " <" + comoName + "> " + (isBuy ? "#$" : "#") + ": " + rest + " $: " + (isQuick ? "AUTO" : price));

        Trans.Req req = isBuy ? (isQuick ? Trans.mkQuickBuy(aid, fid, comoName, rest)
                : Trans.mkSlowBuy(aid, fid, comoName, rest, price))
                : (isQuick ? Trans.mkQuickSell(aid, fid, comoName, rest)
                : Trans.mkSlowSell(aid, fid, comoName, rest, price));

        trh.send(req);
    }

    private void showFirm() {
        String fid = (String) firmComboBox.getSelectedItem();
        new FirmView( trh.getFirm(fid) );
    }

    private void showTabule() {
        String comoName = (String) comoComboBox.getSelectedItem();
        new TabuleView( trh.getTabule(comoName) );
    }

    private void showAllFirms() {
        for (String fid : trh.getFIDsArray()) {
            new FirmView( trh.getFirm(fid) );
        }
    }

    private void showAllTables() {
        for (String comoName : trh.getTabsArray()) {
            new TabuleView( trh.getTabule(comoName) );
        }
    }








}
