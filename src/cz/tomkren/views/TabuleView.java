package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.Tabule;

import javax.swing.*;

public class TabuleView implements ChangeListener {
    private Tabule tabule;
    private JFrame frame;

    private JTextArea textArea;
    private JPanel panel;
    private JLabel comoNameLabel;

    public TabuleView(Tabule t) {
        tabule = t;
        tabule.getChangeInformer().addListener(this);

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

    @Override
    public void onChange() {
        draw();
    }
}
