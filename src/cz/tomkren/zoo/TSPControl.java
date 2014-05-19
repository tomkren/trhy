package cz.tomkren.zoo;

import cz.tomkren.trhy.helpers.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;


public class TSPControl {
    private JButton findOnePathButton;
    private JPanel mainPanel;


    public TSPControl(TSP tsp) {

        JFrame frame = new JFrame("TSPControl");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        findOnePathButton.addActionListener(e -> {
            int[] path = tsp.findPath();
            Log.it("path: "+ Arrays.toString(path) );
        });
    }
}
