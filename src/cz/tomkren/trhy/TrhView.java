package cz.tomkren.trhy;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TrhView implements ChangeListener {

    private Trh    trh;

    private JFrame frame;
    private JLabel tikLabel;
    private JPanel panel;

    private JComboBox firmComboBox;
    private JComboBox comoComboBox;
    private JButton   showFirmButton;
    private JButton   showTabuleButton;
    private JButton   showAllFirmsButton;
    private JButton   showAllTablesButton;

    private JTextArea logTextArea;

    private JComboBox  transFidComboBox;
    private JComboBox  buyOrSellComboBox;
    private JComboBox  quickOrSlowComboBox;
    private JComboBox  transComoComboBox;
    private JTextField priceTextField;
    private JTextField transRestTextField;
    private JButton    sendButton;
    private JLabel transRestLabel;
    private JComboBox transAidComboBox;


    public TrhView(Trh t) {
        trh = t;
        trh.addChangeListener(this);

        frame = new JFrame("TrhView");
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
                try {rest  = Double.parseDouble( (String) transRestTextField.getText() );} catch (NumberFormatException ex) {rest = 0;}
                try {price = Double.parseDouble( (String) priceTextField    .getText() );} catch (NumberFormatException ex) {price= 0;}

                Log.it(  "<"+aid+"> via <"+fid+"> " + (isQuick?"QUICK":"SLOW") +" "+ (isBuy?"BUY":"SELL") +
                        " <"+comoName+"> " + (isBuy?"#$":"#") +": "+ rest + " $: "+(isQuick?"AUTO":price) );

                Trans.Req req;

                if (isBuy) {
                    double money = rest;
                    if (isQuick) { req = Trans.mkQuickBuy(aid, fid, comoName, money);        }
                    else         { req = Trans.mkSlowBuy (aid, fid, comoName, money, price); }
                } else { // sell
                    double num = rest;
                    if (isQuick) { req = Trans.mkQuickSell(aid, fid, comoName, num);         }
                    else         { req = Trans.mkSlowSell (aid, fid, comoName, num, price);  }
                }

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


        ComboBoxModel aidsModel   = new DefaultComboBoxModel( trh.getAIDsArray() );
        ComboBoxModel fidsModel1  = new DefaultComboBoxModel( trh.getFIDsArray() );
        ComboBoxModel fidsModel2  = new DefaultComboBoxModel( trh.getFIDsArray() );
        ComboBoxModel comosModel1 = new DefaultComboBoxModel( trh.getTabsArray() );
        ComboBoxModel comosModel2 = new DefaultComboBoxModel( trh.getTabsArray() );

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

    public void testUpdate() {
        int i = Integer.parseInt(tikLabel.getText());
        tikLabel.setText(Integer.toString(i + 1));
    }

    public static void main(String[] args) {

        Firm kolonial = new Firm(
                "Koloniál Katz", //Penuel Katz
                new Item[]{
                        new Item("$",100000),
                        new Item("Work",5),
                        new Item("Flour",5000),
                        new Item("Pie",100)
                });

        Firm poleAS = new Firm(
                "Pole a.s.",
                new Item[]{
                        new Item("$",1000),
                        new Item("Work",1000),
                        new Item("Flour",5000)
                });

        Trh trh = new Trh();

        new TrhView(trh);

        try {

            trh.addFirm("Penuel Katz", kolonial);
            trh.addFirm("Václav Rolný", poleAS);


        } catch (Trh.TrhException e) {
            Log.it("ERROR! >>> "+e.getMessage());
        }

        //for (int i = 0; i < 10000000; i++) {
        //    tv.testUpdate();
        //}
    }



}
