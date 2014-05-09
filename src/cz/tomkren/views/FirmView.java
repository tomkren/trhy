package cz.tomkren.views;

import cz.tomkren.observer.ChangeListener;
import cz.tomkren.trhy.*;

import javax.swing.*;


public class FirmView implements ChangeListener {

    private Firm firm;

    private JPanel panel;
    private JLabel fidLabel;
    private JTextArea textArea;

    public FirmView(Firm f) {
        firm = f;
        ViewUtils.mkFrameAndRegister(firm.getFirmID(), panel, firm.getChangeInformer(), this);
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
