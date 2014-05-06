package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.Firm;

import javax.swing.*;


public class FirmView implements ChangeListener {

    private Firm firm;

    private JPanel panel;
    private JLabel fidLabel;
    private JTextArea textArea;

    public FirmView(Firm f) {
        firm = f;
        firm.getChangeInformer().addListener(this);

        JFrame frame = new JFrame(firm.getFirmID());
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        draw();
    }

    private void draw () {
        fidLabel.setText(firm.getFirmID());
        textArea.setText(firm.toString());
    }

    @Override
    public void onChange() {
        draw();
    }
}
