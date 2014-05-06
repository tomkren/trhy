package cz.tomkren.views;

import cz.tomkren.observer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Utils {

    public static JFrame mkFrameAndRegister(String name, Container container, ChangeInformerService informer, ChangeListener listener) {

        informer.addListener(listener);

        JFrame frame = new JFrame(name);
        frame.setContentPane(container);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        final ChangeInformerService informer_ = informer;
        final ChangeListener listener_ = listener;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                informer_.removeListener(listener_);
            }
        });

        return frame;
    }
}
