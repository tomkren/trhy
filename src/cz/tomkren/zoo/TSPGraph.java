package cz.tomkren.zoo;


import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TSPGraph {

    private int n;
    private Point[] points;
    private EdgeData[][] matrix;
    
    private double alpha;
    private double beta;

    private Random rand;


    public TSPGraph(Point[] ps, TSP.Opts opts) {
        points = ps;
        n = points.length;
        matrix = new EdgeData[n][n];
        
        alpha = opts.getAlpha();
        beta  = opts.getBeta(); 

        rand = new Random();

        for (int i=0; i<n; i++) {
            Point from = points[i];
            for (int j=i+1; j<n; j++){
                Point to = points[j];
                double eta = 1./eucDistance(from,to);
                matrix[i][j] = new EdgeData(opts.getTauMin(), eta );
            }
        }
    }


    public int[] findPath() {
        int[] path = new int[n];
        Set<Integer> beenThere = new HashSet<>();

        int start = rand.nextInt(n);
        path[0] = start;
        beenThere.add(start);

        int from = start;

        for (int i=1; i<n; i++) {
            int next = nextNode(from, beenThere);
            path[i] = next;
            beenThere.add(next);
            from = next;
        }

        return path;
    }

    private EdgeData get(int i, int j) {
        if (i<j) {
            return matrix[i][j];
        } else {
            return matrix[j][i];
        }
    }

    private int nextNode(int i, Set<Integer> beenThere) {
        double[] p = new double[n];
        double sum = 0;

        for (int j=0; j<n; j++) {
            if (beenThere.contains(j)) {
                p[j] = 0;
            } else {
                EdgeData e = get(i,j);
                double pst = Math.pow(e.getTau(),alpha)
                           * Math.pow(e.getEta(),beta);
                p[j] = pst;
                sum += pst;
            }
        }

        double ball = sum * rand.nextDouble();

        sum = 0;

        for (int k=0; k<n; k++) {

            sum += p[k];

            if(ball <= sum) {
                return k;
            }

        }

        throw new Error("ERR in nextNode...");
    }

    private static double eucDistance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public Point[] getPoints() {
        return points;
    }

    public EdgeInfo[] getEdgeInfos() {
        EdgeInfo[] ret = new EdgeInfo[(n*(n-1))/2];

        int k = 0;

        for (int i=0; i<n; i++) {
            Point from = points[i];
            for (int j=i+1; j<n; j++){
                Point to = points[j];
                ret[k] = new EdgeInfo(from, to, matrix[i][j].getEta());
                k++;
            }
        }

        return ret;
    }


    public static class EdgeData {
        private double pheromone;
        private double weight;

        public EdgeData(double tau, double eta) {
            this.pheromone = tau;
            this.weight = eta;
        }

        public void setPheromone(double pheromone) {
            this.pheromone = pheromone;
        }

        public double getTau() {
            return pheromone;
        }

        public double getEta() {
            return weight;
        }
    }

    public static class EdgeInfo {
        private Point  from;
        private Point  to;
        private double weight;

        private EdgeInfo(Point from, Point to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public Point getFrom() {
            return from;
        }

        public Point getTo() {
            return to;
        }

        public double getWeight() {
            return weight;
        }
    }


}
