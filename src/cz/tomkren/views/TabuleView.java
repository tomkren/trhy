package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.Tabule;

import javax.swing.*;

public class TabuleView implements ChangeListener {
    private Tabule tabule;

    private JTextArea textArea;
    private JPanel panel;
    private JLabel comoNameLabel;
    private JLabel priceInfoLabel;

    public TabuleView(Tabule t) {
        tabule = t;
        ViewUtils.mkFrameAndRegister(tabule.getComoName() + " market table", panel, tabule.getChangeInformer(), this);
        draw();
    }

    private void draw () {
        comoNameLabel.setText(tabule.getComoName());
        priceInfoLabel.setText(tabule.getPriceInfo().toString());

        textArea.setText(tabule.toString());
    }

    @Override
    public void onChange() {
        draw();
    }
}
