package cz.tomkren.trhy;

import javax.swing.*;

/**
 * Created by sekol on 6.5.2014.
 */
public class TabuleView {
    private Tabule tabule;
    private JFrame frame;

    private JTextArea textArea;
    private JPanel panel;
    private JLabel comoNameLabel;

    public TabuleView(Tabule t) {
        tabule = t;

        frame = new JFrame( tabule.getComoName() + " market table" );
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        draw();
    }

    private void draw () {
        comoNameLabel.setText(tabule.getComoName());
        textArea.setText(tabule.toString());
    }
}
