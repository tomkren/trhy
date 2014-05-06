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
        Utils.initFrame(tabule.getComoName() + " market table", panel, tabule.getChangeInformer(), this);
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
