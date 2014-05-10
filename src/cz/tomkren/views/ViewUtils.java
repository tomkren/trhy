package cz.tomkren.views;

import cz.tomkren.observer.*;
import cz.tomkren.trhy.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ViewUtils {

    public static JFrame mkFrameAndRegister(String name, Container container, ChangeInformer informer, ChangeListener listener, Point framePos) {

        informer.addListener(listener);

        JFrame frame = new JFrame(name);
        frame.setContentPane(container);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        final ChangeInformer informer_ = informer;
        final ChangeListener listener_ = listener;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                informer_.removeListener(listener_);
            }
        });

        if (framePos != null) {
            frame.setLocation(framePos);
        }

        return frame;
    }

    public static final boolean USE_SYSTEM_LOOK_AND_FEEL = false;
    public static final boolean USE_NIMBUS_LOOK_AND_FEEL = true;

    public static boolean setLookAndFeel () {
        return setLookAndFeel(USE_NIMBUS_LOOK_AND_FEEL, USE_SYSTEM_LOOK_AND_FEEL);
    }

    public static boolean setLookAndFeel (boolean useNimbus, boolean useSystem) {
        if (useNimbus) { if    (setNimbusLookAndFeel()){return true;} }
        if (useSystem) { return setSystemLookAndFeel();               }
        return true;
    }

    public static boolean setNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.it("Unable to set Nimbus look and feel, sorry.");
            return false;
        }
    }

    public static boolean setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            return true;
        } catch (Exception e) {
            Log.it("Unable to set native look and feel, sorry.");
            return false;
        }
    }
}
