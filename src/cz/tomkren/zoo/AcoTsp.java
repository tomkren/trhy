package cz.tomkren.zoo;


import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class AcoTsp implements TSP.Solver {

    public static final int    NUM_ANTS = 25;
    public static final double RHO      = 0.2;
    public static final double ALPHA    = 1.0;
    public static final double BETA     = 2.0;


    public static AcoTsp mk(Point[] points, double optVal) {
        return new AcoTsp( points, mkDefaultOpts(optVal, points.length) );
    }

    public static Opts mkDefaultOpts(double optVal, double n) {

        double tau_max  = (1./RHO)*(1./optVal);
        double tau_min  = tau_max/(2.*n);

        return new Opts(NUM_ANTS,RHO,ALPHA,BETA,tau_min,tau_max);
    }

    public static class Opts {
        private int    numAnts;
        private double evaporationRate;
        private double alpha;
        private double beta;
        private double tauMax;
        private double tauMin;

        public Opts(int numAnts, double evaporationRate, double alpha, double beta, double tauMax, double tauMin) {
            this.numAnts = numAnts;
            this.evaporationRate = evaporationRate;
            this.alpha = alpha;
            this.beta = beta;
            this.tauMax = tauMax;
            this.tauMin = tauMin;
        }
        public int getNumAnts() {
            return numAnts;
        }
        public double getEvaporationRate() {
            return evaporationRate;
        }
        public double getAlpha() {
            return alpha;
        }
        public double getBeta() {
            return beta;
        }
        public double getTauMax() {
            return tauMax;
        }
        public double getTauMin() {
            return tauMin;
        }
    }



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

    public AcoTsp(Point[] ps, Opts opts) {

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

        init();
    }

    private void init() {
        iteration = 0;
        bestPath = null;
        bestLen = Double.POSITIVE_INFINITY;

        for (int i=0; i<n; i++) {
            Point from = points[i];
            for (int j=i+1; j<n; j++){
                Point to = points[j];
                matrix[i][j] = new EdgeData(tauMax, TSP.eucDistance(from,to) );
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

    public void run(int numIterations, Consumer<TSP.IterationInfo> logFun) {
        init();

        for (int i = 0; i < numIterations; i++) {
            int[] path = doOneIteration();

            String msg = "!: "+ iteration;
            logFun.accept( new TSP.IterationInfo(path ,msg, iteration) );
        }

    }

    private void the2opt(int[] path) {
        //todo Q: Může se stát, že kvůli modifikacím některý dvojice zkontroluju víc než jednou a některý ani jednou?

        int tries = 0;
        boolean modified = true;

        while (modified && tries < 3) {

            modified = false;
            tries++;

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    if (!isOk2opt(path, i, j)) {
                        swap2opt(path, i, j);
                        modified = true;
                    }
                }
            }

        }
    }

    private boolean isOk2opt(int[] path, int i, int j) {
        int from1 = path[i];
        int to1   = path[i+1];
        int from2 = path[j];
        int to2   = path[(j+1)%n];

        double distNow = get( from1, to1   ).dist + get( from2, to2 ).dist;
        double distAlt = get( from1, from2 ).dist + get( to1  , to2 ).dist;

        return distNow < distAlt;
    }

    private void swap2opt(int[] path, int i, int j) {
        int begin = i+1;
        int end   = j;
        int temp;

        while (begin<end) {
            temp        = path[begin];
            path[begin] = path[end];
            path[end]   = temp;

            begin++;
            end--;
        }
    }



    public int[] doOneIteration() {

        int[]  itBestPath = null;
        double itBestLen  = Double.POSITIVE_INFINITY;

        // vygeneruj nové cesty
        for (int i=0; i<numAnts; i++) {
            int[] path = findPath();
            the2opt(path);
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

        boolean useGlobalBest = decideUseGlobalBest(iteration);

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


    private boolean decideUseGlobalBest(int iteration) {

        if (iteration < 25) {
            return false;
        } else if (iteration < 75) {
            return (iteration%5 == 0);
        } else if (iteration < 125) {
            return (iteration%3 == 0);
        } else if (iteration < 250) {
            return (iteration%2 == 0);
        } else {
            return true;
        }

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



    public Point[] getPoints() {
        return points;
    }

    public Point getPoint(int i) {
        return points[i];
    }

    public TSP.EdgeInfo[] getEdgeInfos() {
        TSP.EdgeInfo[] ret = new TSP.EdgeInfo[(n*(n-1))/2];

        int k = 0;

        for (int i=0; i<n; i++) {
            Point from = points[i];
            for (int j=i+1; j<n; j++){
                Point to = points[j];
                ret[k] = new TSP.EdgeInfo(from, to, matrix[i][j].getEta());
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




}
