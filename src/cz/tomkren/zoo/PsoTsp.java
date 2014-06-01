package cz.tomkren.zoo;


import java.awt.*;
import java.util.function.Consumer;

// TODO dodělat!

public class PsoTsp implements PSO.Fitness , TSP.Solver {

    private PSO pso;

    private int n;
    private Point[] points;
    private double[][] matrix;


    public PsoTsp(Point[] ps) {

        points = ps;
        n = points.length;
        matrix = new double[n][n];

        for (int i=0; i<n; i++) {
            Point from = points[i];
            for (int j=i+1; j<n; j++){
                Point to = points[j];
                matrix[i][j] = TSP.eucDistance(from,to);
            }
        }

        PSO.Opts psoOpts = new PSO.Opts(
                pos -> pos[0]*pos[0] + pos[1]*pos[1], // fitness
                25, // numParticles
                n,  // n (dim)
                PSO.mkArr(n, 0.0), // mins
                PSO.mkArr(n, 1.0), // maxs
                0.9, // omega
                1,   // phi_p
                1,   // phi_g
                0.1  // v_max
        );

        pso = new PSO(psoOpts);
    }

    public int[] posToPath(double[] pos) {
        throw new Error("TODO");
    }

    @Override
    public double getFitness(double[] pos) {
        return pathLen(posToPath(pos));
    }

    private double get(int i, int j) {
        if (i<j) { return matrix[i][j]; }
        else     { return matrix[j][i]; }
    }


    @Override
    public void run(int numIterations, Consumer<TSP.IterationInfo> logFun) {
        throw new Error("TODO");
    }

    @Override
    public int[] doOneIteration() {
        throw new Error("TODO");
    }

    @Override
    public int[] findPath() {
        throw new Error("TODO");
    }

    @Override
    public Point[] getPoints() {
        return points;
    }

    @Override
    public Point getPoint(int i) {
        return points[i];
    }

    @Override
    public double pathLen(int[] path) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += get( path[i%n], path[(i+1)%n] );
        }
        return sum;
    }

    @Override
    public TSP.EdgeInfo[] getEdgeInfos() {
        return new TSP.EdgeInfo[0];
    }




}
