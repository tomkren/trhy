package cz.tomkren.zoo;


import java.awt.*;
import java.util.function.Consumer;

public class TSP {

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

    public static class IterationInfo {
        private int[]  path;
        private String msg;
        private int    iteration;

        public IterationInfo(int[] path, String msg, int iteration) {
            this.path = path;
            this.msg = msg;
            this.iteration = iteration;
        }
        public int[] getPath() {
            return path;
        }
        public String getMsg() {
            return msg;
        }
        public int getIteration() {
            return iteration;
        }
    }

    private TspAcoGraph graph;

    public TSP (Opts opts, int[] euc2d) {
        graph = new TspAcoGraph(mkPoints(euc2d), opts);
    }

    public void run(int numIterations, Consumer<IterationInfo> logFun) {
        graph.run(numIterations, logFun);
    }

    private static Point[] mkPoints(int[] euc2d){
        if (euc2d.length % 2 != 0) { throw new Error("Input list length must be even."); }
        int n = euc2d.length / 2;

        Point[] points = new Point[n];

        for (int i=0; i<n; i++) {
            int x = euc2d[i*2];
            int y = euc2d[i*2+1];
            points[i] = new Point(x,y);
        }

        return points;
    }

    public int[] doOneIteration() {
        return graph.doOneIteration();
    }

    public int[] findPath() {
        return graph.findPath();
    }

    public Point[] getPoints() {
        return graph.getPoints();
    }

    public Point getPoint(int i) {
        return graph.getPoint(i);
    }

    public double pathLen(int[] path) {
        return graph.pathLen(path);
    }

    public TspAcoGraph.EdgeInfo[] getEdges() {
        return graph.getEdgeInfos();
    }

    //haxy!!!
    public static final double OPT_VAL_HAX = 423.741;//108159.0;
    public static final double N_HAX       = 30;     //76.0;


    public static final int    NUM_ANTS = 25;
    public static final double RHO      = 0.2;
    public static final double ALPHA    = 1.0;
    public static final double BETA     = 2.0;
    public static final double TAU_MAX  = (1./RHO)*(1./OPT_VAL_HAX);
    public static final double TAU_MIN  = TAU_MAX/(2.*N_HAX);



    public static final Opts DEFAULT_OPTS = new Opts(NUM_ANTS,RHO,ALPHA,BETA,TAU_MIN,TAU_MAX);

    public static Opts mkDefaultOpts(double optVal, double n) {

        double tau_max  = (1./RHO)*(1./optVal);
        double tau_min  = tau_max/(2.*n);

        return new Opts(NUM_ANTS,RHO,ALPHA,BETA,tau_min,tau_max);
    }

    public static void main(String[] args) {

        // oliver30
        TSP test1 = new TSP( mkDefaultOpts(423.741, 30), new int[]{
            54, 67,   54, 62,   37, 84,    41, 94,    2, 99,
             7, 64,   25, 62,   22, 60,    18, 54,    4, 50,
            13, 40,   18, 40,   24, 42,    25, 38,    44, 35,
            41, 26,   45, 21,   58, 35,    62, 32,    82,  7,
            91, 38,   83, 46,   71, 44,    64, 60,    68, 58,
            83, 69,   87, 76,    74, 78,   71, 71,    58, 69
        });



        // pr76.tsp
        TSP test2 = new TSP( mkDefaultOpts(108159.0, 76.0), new int[]{
            3600, 2300,     3100, 3300,     4700, 5750,     5400, 5750,
            5608, 7103,     4493, 7102,     3600, 6950,     3100, 7250,
            4700, 8450,     5400 , 8450,    5610 , 10053,   4492 , 10052,
            3600 , 10800,   3100 , 10950,   4700 , 11650,   5400 , 11650,
            6650 , 10800,   7300 , 10950,   7300 , 7250,    6650 , 6950,
            7300 , 3300,    6650 , 2300,    5400 , 1600,    8350 , 2300,
            7850 , 3300,    9450 , 5750,    10150, 5750,    10358, 7103,
            9243 , 7102,    8350 , 6950,    7850 , 7250,    9450 , 8450,
            10150, 8450,    10360, 10053,   9242 , 10052,   8350 , 10800,
            7850 , 10950,   9450 , 11650,   10150, 11650,   11400, 10800,
            12050, 10950,   12050, 7250,    11400, 6950,    12050, 3300,
            11400, 2300,    10150, 1600,    13100, 2300,    12600, 3300,
            14200, 5750,    14900, 5750,    15108, 7103,    13993, 7102,
            13100, 6950,    12600, 7250,    14200, 8450,    14900, 8450,
            15110, 10053,   13992, 10052,   13100, 10800,   12600, 10950,
            14200, 11650,   14900, 11650,   16150, 10800,   16800, 10950,
            16800, 7250,    16150, 6950,    16800, 3300,    16150, 2300,
            14900, 1600,    19800, 800,     19800, 10000,   19800, 11900,
            19800, 12200,   200  , 12200,   200  , 1100,    200  , 800
        });

        //tsp225
        TSP test3 = new TSP( mkDefaultOpts(391900, 225), new int[] {
            15542,15065,   37592,16465,   18392,15065,   20542,15065,   20542,17165,
            22642,17165,   22642,18615,   22642,20715,   22642,23565,   22642,26415,
            22642,29265,   22642,31415,   22642,33565,   20542,33565,   19092,33565,
            19092,32815,   17692,32815,   17692,29965,   15542,29965,   15542,32815,
            15542,35665,   18392,35665,   21942,35665,   24092,35665,   26942,35665,
            29042,35665,   38742,13615,   31892,35665,   31892,33565,   31892,32815,
            31892,29965,   29792,29965,   29042,32815,   29042,33565,   29792,32815,
            25492,33565,   25492,31415,   25492,29265,   25492,27165,   25492,24315,
            25492,22165,   25492,19315,   25492,17165,   27642,17165,   29642,15065,
            27642,15065,   37592,15065,   30892,15065,   35492,16465,   33842,17465,
            35492,17465,   33842,20015,   33842,22165,   35492,22165,   35492,20015,
            36192,20015,   36192,18615,   38342,18615,   38342,17915,   40442,17915,
            40442,18615,   41892,18615,   41892,20015,   43292,20015,   43292,22165,
            41892,22165,   41892,23565,   39742,23565,   39742,24315,   37592,24315,
            37592,25715,   36892,25715,   36892,26415,   34742,26415,   34742,27865,
            33642,27865,   33642,32815,   34742,32815,   34742,34265,   36892,34265,
            36892,35365,   41892,35365,   41892,34265,   43292,34265,   43292,35665,
            44742,35665,   44742,32115,   44742,29265,   43292,29265,   43292,31415,
            41892,31415,   41892,32115,   39742,32115,   39742,33365,   37592,33365,
            37592,32115,   36192,32115,   36192,29965,   37592,29965,   37592,28565,
            39742,28565,   39742,27165,   41892,27165,   41892,26415,   43992,26415,
            43992,25015,   45442,25015,   45442,24315,   46142,24315,   46142,21465,
            46142,19315,   44742,19315,   44742,17915,   43992,17915,   43992,16765,
            41992,16765,   41992,15065,   43992,15065,   45442,15065,   47592,15065,
            47592,17165,   49692,17165,   49692,19315,   49692,21465,   49692,24315,
            49692,27165,   49692,29265,   49692,31715,   49692,33565,   47042,33565,
            47042,35665,   49692,35665,   34742,15065,   53992,35665,   56092,35665,
            58942,35665,   58942,34265,   60392,34265,   61092,34265,   61092,33565,
            61092,32115,   62492,32115,   62492,27865,   61092,27865,   61092,25715,
            58942,25715,   58942,25015,   57542,25015,   56092,25015,   54292,25015,
            54292,26415,   56092,26415,   57542,26415,   57542,27165,   58242,27165,
            58242,28565,   59642,28565,   56092,33565,   59642,31415,   58242,31415,
            58242,32115,   57542,32115,   57542,33565,   52542,33565,   52542,31415,
            52542,29965,   52542,28165,   52542,23315,   52542,21465,   52542,19315,
            52542,17165,   54692,17165,   54692,15065,   56842,15065,   47592,16065,
            60392,15065,   62492,15065,   62492,13615,   59642,13615,   57542,13615,
            55392,13615,   53242,13615,   57542,35665,   48992,13615,   46842,13615,
            44742,13615,   42592,13615,   40442,13615,   37042,13615,   36192,15065,
            34042,13615,   32642,13615,   30192,13615,   27642,13615,   25492,13615,
            31592,13615,   21242,13615,   19092,13615,   33892,15065,   15542,13615,
            62492,29965,   31892,32165,   15542,31415,   31192,35665,   35542,13615,
            31892,31415,   36292,16465,   25492,35665,   38342,33365,   44742,33565,
            47042,34565,   52542,25015,   54692,33565,   52542,26115,   52542,35665,
            33642,29865,   33642,31315,   29342,13615,   33642,30615,   42592,26415,
            39142,35365,   48292,33565,   42992,16765,   33092,15065,   36842,15065
        });

        new TSPView(test3);


    }

}
