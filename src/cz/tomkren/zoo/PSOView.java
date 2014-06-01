package cz.tomkren.zoo;

import cz.tomkren.trhy.helpers.Log;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// todo dodržet DRY --- zkopčený z TSPView !!!!

public class PSOView {

    private PSO pso;
    private MyFrame myFrame;

    private int x_dimIndex;
    private int y_dimIndex;

    private double x_min;
    private double y_min;
    private double x_max;
    private double y_max;


    public PSOView (PSO pso, int x_dimIndex, int y_dimIndex) {
        this.pso = pso;
        this.x_dimIndex = x_dimIndex;
        this.y_dimIndex = y_dimIndex;

        x_min = pso.getMins()[x_dimIndex];
        y_min = pso.getMins()[y_dimIndex];
        x_max = pso.getMaxs()[x_dimIndex];
        y_max = pso.getMaxs()[y_dimIndex];

        new PSOControl(pso, this);

        run();
    }

    private void run() {
        SwingUtilities.invokeLater(() -> {
            myFrame = new MyFrame();
            myFrame.setVisible(true);
            //SwingUtilities.invokeLater(() -> myFrame.draw());
        });
    }

    public void draw() {
        myFrame.draw();
    }

    private static void drawCircle(Graphics2D g, Point p, int r, Color fill, Color line) {
        int x = p.x -r/2;
        int y = p.y -r/2;
        g.setColor(fill);
        g.fillOval(x, y, r, r);
        g.setColor(line);
        g.drawOval(x, y, r, r);
    }



    private class MyPanel extends JPanel {

        public void draw() {
            Graphics2D g = (Graphics2D) getGraphics();
            super.paintComponent(g);
            doDrawing(g);
        }

        private void doDrawing(Graphics2D g) {

            // todo tady se kreslí

            Point size = getMySize();

            //g.setColor(Color.white);
            //g.fillRect(0,0,size.x,size.y);

            double box_x = size.x/4;
            double box_y = size.y/4;
            double box_w = size.x/2;
            double box_h = size.y/2;

            double alpha_x = box_w / (x_max - x_min);
            double beta_x  = box_x - alpha_x*x_min;

            double alpha_y = box_h / (y_max - y_min);
            double beta_y  = box_y - alpha_y*y_min;

            g.setColor(Color.black);
            g.drawRect((int) box_x, (int) box_y, (int) box_w, (int) box_h);

            int i = 0;
            for (PSO.Particle p : pso.getParticles()) {

                double x = p.getPos()[x_dimIndex] * alpha_x + beta_x;
                double y = p.getPos()[y_dimIndex] * alpha_y + beta_y;

                double v_x = (p.getPos()[x_dimIndex] + p.getV()[x_dimIndex]) * alpha_x + beta_x;
                double v_y = (p.getPos()[y_dimIndex] + p.getV()[y_dimIndex]) * alpha_y + beta_y;


                g.setColor(Color.gray);
                g.drawLine((int)x,(int)y,(int)(v_x),(int)(v_y));

                Color c = new Color( 128+(123+i*87)%128  , 128+(7+i*13)%128 , 128+(207+i*7011)%128 );
                g.setColor(c);
                drawCircle(g, new Point((int)x, (int)y) , 7, c, Color.black);

                i++;
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
            doDrawing((Graphics2D) g);
        }

        public void myPaint(Consumer<Graphics2D> f) {

            Graphics2D g = (Graphics2D) getGraphics();

            super.paintComponent(g);
            doDrawing(g);
            f.accept(g);
        }
    }


    private class MyFrame extends JFrame {
        private MyPanel myPanel;

        public MyFrame() {
            setTitle("TSPView");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            myPanel = new MyPanel();
            add(myPanel);
            setSize(720, 720);
            setLocationRelativeTo(null);
            //setIgnoreRepaint(true);

        }

        public void myPaint(Consumer<Graphics2D> f) {
            myPanel.myPaint(f);
        }

        public void draw() {
            myPanel.draw();
        }
    }
}
