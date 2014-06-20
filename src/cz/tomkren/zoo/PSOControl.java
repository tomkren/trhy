package cz.tomkren.zoo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PSOControl {
    private JButton doOneIterationButton;
    private JPanel mainPanel;
    private JButton initializeButton;
    private JButton runButton;
    private JTextField numItersTextField;


    public PSOControl(PSO pso, PSOView psoView) {

        JFrame frame = new JFrame("PSO Control");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        doOneIterationButton.addActionListener(e -> {
            pso.doOneIteration();
            psoView.draw();
        });
        initializeButton.addActionListener(e -> {
            pso.init();
            psoView.draw();
        });

        runButton.addActionListener(e -> {
            int numIters = Integer.parseInt(numItersTextField.getText());;
            for (int i = 0; i < numIters; i++) {
                pso.doOneIteration();
                psoView.draw();
            }
        });
    }
}
