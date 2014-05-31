package cz.tomkren.zoo;

import cz.tomkren.trhy.helpers.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;


public class TSPControl {
    private JButton findOnePathButton;
    private JPanel mainPanel;
    private JButton doOneIterationButton;
    private JButton runButton;
    private JTextField numItersTextField;

    public TSPControl(TSP tsp, TSPView tspView) {

        JFrame frame = new JFrame("TSPControl");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        findOnePathButton.addActionListener(e -> {
            tspView.drawPath(tsp.findPath(), Color.black, true);
        });
        doOneIterationButton.addActionListener(e -> {
            int[] updatePath = tsp.doOneIteration();
            tspView.drawPath(updatePath, Color.red, true);
        });
        runButton.addActionListener(e -> {
            int numIters = Integer.parseInt(numItersTextField.getText());
            tsp.run(numIters, iterationInfo -> {
                Log.it(iterationInfo.getMsg());
                tspView.drawPath(iterationInfo.getPath(), Color.red, numIters == iterationInfo.getIteration());
            });
        });
    }
}
