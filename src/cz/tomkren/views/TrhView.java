package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.*;

import javax.swing.*;


public class TrhView implements ChangeListener {

    private Trh trh;
    private boolean autoUpdate;


    private JLabel tikLabel;
    private JPanel panel;

    private JComboBox<String> firmComboBox;
    private JComboBox<String> comoComboBox;
    private JButton   showFirmButton;
    private JButton   showTabuleButton;
    private JButton   showAllFirmsButton;
    private JButton   showAllTablesButton;

    private JTextArea logTextArea;

    private JComboBox<String> transFidComboBox;
    private JComboBox  buyOrSellComboBox;
    private JComboBox  quickOrSlowComboBox;
    private JComboBox<String> transComoComboBox;
    private JTextField priceTextField;
    private JTextField transRestTextField;
    private JButton sendTransactionButton;
    private JLabel transRestLabel;
    private JComboBox<String> transAidComboBox;
    private JButton sendRandTransButton;
    private JSpinner nSpinner;
    private JCheckBox includeMachinesCheckBox;


    public TrhView(Trh t) {
        trh = t;
        autoUpdate = true;
        final TrhTester trhTester = new TrhTester(t);
        ViewUtils.mkFrameAndRegister("TrhView", panel, trh.getChangeInformer(), this);
        draw();

        showFirmButton.addActionListener(e -> {
            String fid = (String) firmComboBox.getSelectedItem();
            new FirmView( trh.getFirm(fid) );
        });

        showTabuleButton.addActionListener(e -> {
            String comoName = (String) comoComboBox.getSelectedItem();
            new TabuleView( trh.getTabule(comoName) );
        });

        showAllFirmsButton.addActionListener(e -> {
            for (String fid : trh.getFIDsArray()) {
                new FirmView( trh.getFirm(fid) );
            }
        });

        showAllTablesButton.addActionListener(e -> {
            for (String comoName : trh.getTabsArray()) {
                new TabuleView( trh.getTabule(comoName) );
            }
        });

        sendTransactionButton.addActionListener(e -> {
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
        });

        buyOrSellComboBox.addActionListener(e -> {
            boolean isBuy = "BUY".equals(buyOrSellComboBox.getSelectedItem());
            transRestLabel.setText( isBuy ? "#$:" : "#:" );
        });

        quickOrSlowComboBox.addActionListener(e -> {
            boolean isQuick = "QUICK".equals(quickOrSlowComboBox.getSelectedItem());
            priceTextField.setEnabled(!isQuick);
        });

        nSpinner.setValue(1000);
        sendRandTransButton.addActionListener(e -> {

            int n = (Integer) nSpinner.getValue();
            boolean includeMachines = includeMachinesCheckBox.isSelected();

            //Log.it("Sending "+ n +" random transactions, yeehaa!");

            doWithoutRedrawsThenRedraw(()->{
                boolean testResult = trhTester.sendRandomTrans(n,includeMachines);
                Log.it("Send "+n+" rand trans test result: "+(testResult?"OK":"KO!!!!!!!!!!!!!!!!"));
            });
        });
    }

    private void doWithoutRedrawsThenRedraw (Utils.VoidAction a) {
        setAutoUpdate(false);
        a.doIt();
        setAutoUpdate(true);
        draw();
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

    @Override
    public void onChange() {
        if (autoUpdate) {
            draw();
        }
    }

    private static final boolean USE_SYSTEM_LOOK_AND_FEEL = false;
    private static final boolean USE_NIMBUS_LOOK_AND_FEEL = true;

    private static void setLookAndFeel () {
        if (USE_SYSTEM_LOOK_AND_FEEL) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Log.it("Unable to set native look and feel, sorry.");
            }
        }

        if (USE_NIMBUS_LOOK_AND_FEEL) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                Log.it("Unable to set Nimbus look and feel, sorry.");
            }
        }
    }

    public static void main(String[] args) {

        setLookAndFeel();

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


}
