package cz.tomkren.zoo;


import java.awt.*;

public class TSP {

    public static class Opts {
        private double evaporationRate;
        private double alpha;
        private double beta;
        private double tauMax;
        private double tauMin;

        public Opts(double evaporationRate, double alpha, double beta, double tauMax, double tauMin) {
            this.evaporationRate = evaporationRate;
            this.alpha = alpha;
            this.beta = beta;
            this.tauMax = tauMax;
            this.tauMin = tauMin;
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

    private TSPGraph graph;

    public TSP (Opts opts, int[] euc2d) {
        graph = new TSPGraph(mkPoints(euc2d), opts);


    }

    private Point[] mkPoints(int[] euc2d){
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

    public int[] findPath() {
        return graph.findPath();
    }

    public Point[] getPoints() {
        return graph.getPoints();
    }

    public TSPGraph.EdgeInfo[] getEdges() {
        return graph.getEdgeInfos();
    }

    //haxy!!!
    public static final double OPT_VAL_HAX = 423.741;//108159.0;
    public static final double N_HAX       = 30;//76.0;


    public static final double RHO      = 0.2;
    public static final double ALPHA    = 1.0;
    public static final double BETA     = 2.0;
    public static final double TAU_MAX  = (1./RHO)*(1./OPT_VAL_HAX);
    public static final double TAU_MIN  = TAU_MAX/(2.*N_HAX);



    public static final Opts DEFAULT_OPTS = new Opts(RHO,ALPHA,BETA,TAU_MIN,TAU_MAX);

    public static void main(String[] args) {

        TSP test = new TSP(DEFAULT_OPTS, new int[]{
            54, 67,   54, 62,   37, 84,    41, 94,
            2, 99,     7, 64,   25, 62,    22, 60,
            18, 54,    4, 50,   13, 40,    18, 40,
            24, 42,   25, 38,   44, 35,    41, 26,
            45, 21,   58, 35,   62, 32,    82,  7,
            91, 38,   83, 46,   71, 44,    64, 60,
            68, 58,   83, 69,   87, 76,    74, 78,
            71, 71,   58, 69
        });

        new TSPView(test);

        // pr76.tsp
        /*TSP test = new TSP(DEFAULT_OPTS, new int[]{
                3600, 2300,
                3100, 3300,
                4700, 5750,
                5400, 5750,
                5608, 7103,
                4493, 7102,
                3600, 6950,
                3100, 7250,
                4700, 8450,
                5400 , 8450,
                5610 , 10053,
                4492 , 10052,
                3600 , 10800,
                3100 , 10950,
                4700 , 11650,
                5400 , 11650,
                6650 , 10800,
                7300 , 10950,
                7300 , 7250,
                6650 , 6950,
                7300 , 3300,
                6650 , 2300,
                5400 , 1600,
                8350 , 2300,
                7850 , 3300,
                9450 , 5750,
                10150, 5750,
                10358, 7103,
                9243 , 7102,
                8350 , 6950,
                7850 , 7250,
                9450 , 8450,
                10150, 8450,
                10360, 10053,
                9242 , 10052,
                8350 , 10800,
                7850 , 10950,
                9450 , 11650,
                10150, 11650,
                11400, 10800,
                12050, 10950,
                12050, 7250,
                11400, 6950,
                12050, 3300,
                11400, 2300,
                10150, 1600,
                13100, 2300,
                12600, 3300,
                14200, 5750,
                14900, 5750,
                15108, 7103,
                13993, 7102,
                13100, 6950,
                12600, 7250,
                14200, 8450,
                14900, 8450,
                15110, 10053,
                13992, 10052,
                13100, 10800,
                12600, 10950,
                14200, 11650,
                14900, 11650,
                16150, 10800,
                16800, 10950,
                16800, 7250,
                16150, 6950,
                16800, 3300,
                16150, 2300,
                14900, 1600,
                19800, 800,
                19800, 10000,
                19800, 11900,
                19800, 12200,
                200  , 12200,
                200  , 1100,
                200  , 800
        });*/



    }

}
