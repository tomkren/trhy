package cz.tomkren.trhy;

import javax.swing.*;


public class FirmView {

    private Firm firm;

    private JFrame frame;

    private JPanel panel;
    private JLabel fidLabel;
    private JTextArea textArea;

    public FirmView(Firm f) {
        firm = f;

        frame = new JFrame(firm.getFirmID());
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
}
