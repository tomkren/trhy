package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.*;

import javax.swing.*;


public class TrhView implements ChangeListener {

    private static final boolean USE_SYSTEM_LOOK_AND_FEEL = false;

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


    public TrhView(Trh t) {
        trh = t;
        autoUpdate = true;
        final TrhTester trhTester = new TrhTester(t);
        Utils.mkFrameAndRegister("TrhView", panel, trh.getChangeInformer(), this);
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
            Log.it("Sending "+ n +" random transactions, yeehaa!");

            setAutoUpdate(false);
            trhTester.sendRandomTrans(n);
            setAutoUpdate(true);
            draw();
        });
    }

    public void draw() {

        tikLabel.setText( Integer.toString(trh.getTik()) );

        ComboBoxModel<String> aidsModel   = new DefaultComboBoxModel<>( trh.getAIDsArray() );
        ComboBoxModel<String> fidsModel1  = new DefaultComboBoxModel<>( trh.getFIDsArray() );
        ComboBoxModel<String> fidsModel2  = new DefaultComboBoxModel<>( trh.getFIDsArray() );
        ComboBoxModel<String> comosModel1 = new DefaultComboBoxModel<>( trh.getTabsArray() );
        ComboBoxModel<String> comosModel2 = new DefaultComboBoxModel<>( trh.getTabsArray() );

        transAidComboBox .setModel(aidsModel);
        transFidComboBox .setModel(fidsModel1);
        firmComboBox     .setModel(fidsModel2);
        comoComboBox     .setModel(comosModel1);
        transComoComboBox.setModel(comosModel2);

        StringBuilder sb = new StringBuilder();
        for (final String str : trh.getLog()) {
            sb.append(str).append("\n");
        }
        logTextArea.setText(sb.toString());
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

    public static void main(String[] args) {

        if (USE_SYSTEM_LOOK_AND_FEEL) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Log.it("Unable to set native look and feel, sorry.");
            }
        }

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
