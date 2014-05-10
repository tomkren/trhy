package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.Tabule;

import javax.swing.*;
import java.awt.*;

public class TabuleView implements ChangeListener {
    private Tabule tabule;

    private JTextArea textArea;
    private JPanel panel;
    private JLabel comoNameLabel;
    private JLabel priceInfoLabel;

    private static FramePosouvac posouvac = new FramePosouvac(TrhView.WIDTH_HAX,461, 220);

    public TabuleView(Tabule t) {
        tabule = t;
        ViewUtils.mkFrameAndRegister(tabule.getComoName() + " table", panel, tabule.getChangeInformer(), this, posouvac.nextFramePos() );
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
