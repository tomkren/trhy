package cz.tomkren.trhy;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TrhView implements ChangeListener {

    private Trh    trh;

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
    private JButton    sendButton;
    private JLabel transRestLabel;
    private JComboBox<String> transAidComboBox;


    public TrhView(Trh t) {
        trh = t;
        trh.addChangeListener(this);

        JFrame frame = new JFrame("TrhView");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        showFirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fid = (String) firmComboBox.getSelectedItem();
                new FirmView( trh.getFirm(fid) );
            }
        });

        showTabuleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String comoName = (String) comoComboBox.getSelectedItem();
                new TabuleView( trh.getTabule(comoName) );
            }
        });

        showAllFirmsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (String fid : trh.getFIDsArray()) {
                    new FirmView( trh.getFirm(fid) );
                }
            }
        });

        showAllTablesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (String comoName : trh.getTabsArray()) {
                    new TabuleView( trh.getTabule(comoName) );
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String  aid      = (String) transAidComboBox.getSelectedItem();
                String  fid      = (String) transFidComboBox.getSelectedItem();
                String  comoName = (String) transComoComboBox.getSelectedItem();
                boolean isBuy    = "BUY"  .equals(buyOrSellComboBox.getSelectedItem());
                boolean isQuick  = "QUICK".equals(quickOrSlowComboBox.getSelectedItem());
                double  rest, price;
                try {rest  = Double.parseDouble(transRestTextField.getText());} catch (NumberFormatException ex) {rest = 0;}
                try {price = Double.parseDouble(priceTextField    .getText());} catch (NumberFormatException ex) {price= 0;}

                Log.it(  "<"+aid+"> via <"+fid+"> " + (isQuick?"QUICK":"SLOW") +" "+ (isBuy?"BUY":"SELL") +
                        " <"+comoName+"> " + (isBuy?"#$":"#") +": "+ rest + " $: "+(isQuick?"AUTO":price) );

                Trans.Req req = isBuy ? ( isQuick ? Trans.mkQuickBuy (aid, fid, comoName, rest)
                                                  : Trans.mkSlowBuy  (aid, fid, comoName, rest, price) )
                                      : ( isQuick ? Trans.mkQuickSell(aid, fid, comoName, rest)
                                                  : Trans.mkSlowSell (aid, fid, comoName, rest, price) ) ;

                trh.send(req);
            }
        });

        buyOrSellComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isBuy = "BUY".equals(buyOrSellComboBox.getSelectedItem());
                transRestLabel.setText( isBuy ? "#$:" : "#:" );
            }
        });

        quickOrSlowComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isQuick = "QUICK".equals(quickOrSlowComboBox.getSelectedItem());
                priceTextField.setEnabled(!isQuick);
            }
        });
    }

    public void draw() {

        tikLabel.setText( Integer.toString(trh.getTik()) );

        ComboBoxModel<String> aidsModel   = new DefaultComboBoxModel<String>( trh.getAIDsArray() );
        ComboBoxModel<String> fidsModel1  = new DefaultComboBoxModel<String>( trh.getFIDsArray() );
        ComboBoxModel<String> fidsModel2  = new DefaultComboBoxModel<String>( trh.getFIDsArray() );
        ComboBoxModel<String> comosModel1 = new DefaultComboBoxModel<String>( trh.getTabsArray() );
        ComboBoxModel<String> comosModel2 = new DefaultComboBoxModel<String>( trh.getTabsArray() );

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

    @Override
    public void onChange() {
        draw();
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Log.it("Unable to set native look and feel, sorry.");
        }

        Trh trh = new Trh();
        new TrhView(trh);

        try {
            trh.addFirm("Penuel Katz" , Firm.Examples.mkKolonialKatz());
            trh.addFirm("Václav Rolný", Firm.Examples.mkPoleAS());
        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> "+e.getMessage());
        }

    }


}
