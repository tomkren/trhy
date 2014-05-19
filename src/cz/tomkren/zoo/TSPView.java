package cz.tomkren.zoo;


import javax.swing.*;
import java.awt.*;

public class TSPView {

    private TSP tsp;

    public TSPView (TSP tsp) {
        this.tsp = tsp;
        new TSPControl(tsp);
        run();
    }


    private void run() {
        SwingUtilities.invokeLater(() -> {
            MyFrame ps = new MyFrame();
            ps.setVisible(true);
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
        int x1 = (int)( alpha * e.getFrom().x );
        int y1 = (int)( alpha * e.getFrom().y );
        int x2 = (int)( alpha * e.getTo().x   );
        int y2 = (int)( alpha * e.getTo().y   );
        g.setColor(Color.lightGray);
        g.drawLine(x1,y1,x2,y2);
    }

    private class MyPanel extends JPanel {

        private void doDrawing(Graphics gr) {
            Graphics2D g = (Graphics2D) gr;
            double alpha = (double)(getMySize().x-10) / (double)getMaxCorner();

            for (TSPGraph.EdgeInfo e : tsp.getEdges()) {
                drawLine(g, alpha, e);
            }

            for (Point p : tsp.getPoints()){
                drawCircle(g, alpha,p, 6, Color.gray, Color.black);
            }
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
            doDrawing(g);
        }
    }


    private class MyFrame extends JFrame {
        public MyFrame() {
            setTitle("TSPView");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            add(new MyPanel());
            setSize(720, 720);
            setLocationRelativeTo(null);
        }
    }

}


