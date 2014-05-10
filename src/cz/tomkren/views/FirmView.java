package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.*;

import javax.swing.*;
import java.awt.*;


public class FirmView implements ChangeListener {

    private Firm firm;

    private JPanel panel;
    private JLabel fidLabel;
    private JTextArea textArea;

    private static FramePosouvac posouvac = new FramePosouvac(TrhView.WIDTH_HAX,0, 220);

    public FirmView(Firm f) {
        firm = f;
        ViewUtils.mkFrameAndRegister(firm.getFirmID(), panel, firm.getChangeInformer(), this, posouvac.nextFramePos());
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
