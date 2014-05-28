package cz.tomkren.zoo;


import cz.tomkren.trhy.helpers.Log;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class TSPView {

    private TSP tsp;
    private MyFrame myFrame;

    public TSPView (TSP tsp) {
        this.tsp = tsp;
        new TSPControl(tsp, this);
        run();
    }

    public void drawPath (int[] path, Color c) {

        Log.it("path (len = "+ tsp.pathLen(path) +") :" + Arrays.toString(path));

        myFrame.myPaint( (g,alpha) -> drawPath(g, alpha, path, c) );

    }

    private void drawPath (Graphics2D g, double alpha, int[] path, Color c) {
        int n = path.length;
        for (int i = 0; i < n; i++) {
            Point from = tsp.getPoint(path[i%n]);
            Point to   = tsp.getPoint(path[(i+1)%n]);
            drawLine(g, alpha, from, to, c);
        }

    }


    private void run() {
        SwingUtilities.invokeLater(() -> {
            myFrame = new MyFrame();
            myFrame.setVisible(true);
        });
    }

    private int getMaxCorner() {
        int max = 0;
        for (Point p: tsp.getPoints()) {
            if (p.x > max) { max = p.x; }
            if (p.y > max) { max = p.y; }
        }
        return max;
    }

    private static void drawCircle(Graphics2D g, double alpha, Point p, int r, Color fill, Color line) {
        int x = (int)( alpha * (p.x ) )-r/2;
        int y = (int)( alpha * (p.y ) )-r/2;
        g.setColor(fill);
        g.fillOval(x, y, r, r);
        g.setColor(line);
        g.drawOval(x, y, r, r);
    }

    private void drawLine(Graphics2D g, double alpha, TSPGraph.EdgeInfo e) {
        drawLine(g,alpha, e.getFrom(), e.getTo(), Color.lightGray);
    }

    private void drawLine(Graphics2D g, double alpha, Point from, Point to, Color c) {
        int x1 = (int)( alpha * from.x );
        int y1 = (int)( alpha * from.y );
        int x2 = (int)( alpha * to.x   );
        int y2 = (int)( alpha * to.y   );
        g.setColor(c);
        g.drawLine(x1,y1,x2,y2);
    }

    private class MyPanel extends JPanel {

        private void doDrawing(Graphics2D g, double alpha) {

            for (TSPGraph.EdgeInfo e : tsp.getEdges()) {
                drawLine(g, alpha, e);
            }

            for (Point p : tsp.getPoints()){
                drawCircle(g, alpha,p, 6, Color.gray, Color.black);
            }
        }

        private double getAlpha() {
            return (double)(getMySize().x-10) / (double)getMaxCorner();
        }

        private Point getMySize() {
            Dimension size = getSize();
            Insets insets = getInsets();
            int w = size.width - insets.left - insets.right;
            int h = size.height - insets.top - insets.bottom;
            return new Point(w,h);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            doDrawing((Graphics2D) g, getAlpha());
        }

        public void myPaint(BiConsumer<Graphics2D,Double> f) {

            Graphics2D g = (Graphics2D) getGraphics();
            double alpha = getAlpha();

            super.paintComponent(g);
            doDrawing(g, alpha );
            f.accept( g, alpha );
        }
    }


    private class MyFrame extends JFrame {
        private MyPanel myPanel;

        public MyFrame() {
            setTitle("TSPView");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            myPanel = new MyPanel();
            add(myPanel);
            setSize(720, 800);
            setLocationRelativeTo(null);
        }

        public void myPaint(BiConsumer<Graphics2D,Double> f) {
            myPanel.myPaint(f);
        }
    }

}


