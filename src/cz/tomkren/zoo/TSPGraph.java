package cz.tomkren.zoo;


import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TSPGraph {

    private int n;
    private Point[] points;
    private EdgeData[][] matrix;

    private int    numAnts;
    private double alpha;
    private double beta;
    private double tauMin;
    private double tauMax;
    private double one_minus_rho;

    private Random rand;

    private int    iteration;
    private int[]  bestPath;
    private double bestLen;

    public TSPGraph(Point[] ps, TSP.Opts opts) {
        points = ps;
        n = points.length;
        matrix = new EdgeData[n][n];

        numAnts       = opts.getNumAnts();
        alpha         = opts.getAlpha();
        beta          = opts.getBeta();
        tauMin        = opts.getTauMin();
        tauMax        = opts.getTauMax();
        one_minus_rho = 1-opts.getEvaporationRate();

        rand = new Random();

        iteration = 0;
        bestPath = null;
        bestLen = Double.POSITIVE_INFINITY;

        for (int i=0; i<n; i++) {
            Point from = points[i];
            for (int j=i+1; j<n; j++){
                Point to = points[j];
                matrix[i][j] = new EdgeData(opts.getTauMin(), eucDistance(from,to) );
            }
        }
    }

    private EdgeData get(int i, int j) {
        if (i<j) {
            return matrix[i][j];
        } else {
            return matrix[j][i];
        }
    }

    public int[] doOneIteration() {

        int[]  itBestPath = null;
        double itBestLen  = Double.POSITIVE_INFINITY;

        // vygeneruj nové cesty
        for (int i=0; i<numAnts; i++) {
            int[] path = findPath();
            double len = pathLen(path);

            if (len < itBestLen) {
                itBestPath = path;
                itBestLen  = len;
            }
        }

        if (itBestLen < bestLen) {
            bestPath = itBestPath;
            bestLen  = itBestLen;
        }

        // evaporate pheromones
        for (int i=0; i<n; i++) {
            for (int j=i+1; j<n; j++){
                EdgeData data = matrix[i][j];
                data.setPheromone( data.getTau()*one_minus_rho );
            }
        }

        // update along best path

        boolean useGlobalBest;

        if (iteration < 25) {
            useGlobalBest = false;
        } else if (iteration < 75) {
            useGlobalBest = (iteration%5 == 0);
        } else if (iteration < 125) {
            useGlobalBest = (iteration%3 == 0);
        } else if (iteration < 250) {
            useGlobalBest = (iteration%2 == 0);
        } else {
            useGlobalBest = true;
        }

        double  delta;
        int[]   updatePath;
        if (useGlobalBest) {
            delta      = 1.0 / bestLen;
            updatePath = bestPath;
        } else {
            delta      = 1.0 / itBestLen;
            updatePath = itBestPath;
        }
        for (int i=0; i<n; i++) {
            EdgeData data = get( updatePath[i%n], updatePath[(i+1)%n] );
            data.setPheromone( data.getTau()+delta );
        }

        // put in min max range
        for (int i=0; i<n; i++) {
            for (int j=i+1; j<n; j++){
                EdgeData data = matrix[i][j];
                data.setPheromone( checkInRange(data.getTau()) );
            }
        }

        iteration++;

        return updatePath;
    }


    private double checkInRange(double tau) {
        if (tau < tauMin) {return tauMin;}
        if (tau > tauMax) {return tauMax;}
        return tau;
    }

    public double pathLen (int[] path) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += get( path[i%n], path[(i+1)%n] ).getDist();
        }
        return sum;
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

    public Point getPoint(int i) {
        return points[i];
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
        private double eta;
        private double dist;

        public EdgeData(double tau, double dist) {
            this.pheromone = tau;
            this.dist = dist;
            this.eta = 1.0 / dist;
        }

        public void setPheromone(double pheromone) {
            this.pheromone = pheromone;
        }

        public double getTau() {
            return pheromone;
        }

        public double getEta() {
            return eta;
        }

        public double getDist() {
            return dist;
        }
    }

    public static class EdgeInfo {
        private Point  from;
        private Point  to;
        private double eta;

        private EdgeInfo(Point from, Point to, double eta) {
            this.from = from;
            this.to = to;
            this.eta = eta;
        }

        public Point getFrom() {
            return from;
        }

        public Point getTo() {
            return to;
        }

        public double getEta() {
            return eta;
        }
    }


}
