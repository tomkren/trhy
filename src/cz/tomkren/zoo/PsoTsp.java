package cz.tomkren.zoo;


import cz.tomkren.trhy.helpers.Log;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

// TODO dodělat!

public class PsoTsp implements PSO.Fitness , TSP.Solver {

    public static PsoTsp mk(Point[] ps) {
        return new PsoTsp(ps);
    }

    public static void main(String[] args) {
        Log.it("-- PsoTsp TESTING --");
    }

    private PSO pso;

    private int n;
    private Point[] points;
    private double[][] matrix;

    //hax
    PSOView psoView1, psoView2;

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
                this, // fitness
                42, // numParticles
                n,  // n (dim)
                PSO.mkArr(n, 0.0), // mins
                PSO.mkArr(n, 1.0), // maxs
                0.9,  // omega
                1.5,  // phi_p
                0.5, // phi_g
                0.5,  // v_max
                0.05  //phi_rand
        );

        Random rand = new Random();

        pso = new PSO(psoOpts);
        psoView1 = new PSOView(pso,rand.nextInt(n),rand.nextInt(n));
        psoView2 = new PSOView(pso,rand.nextInt(n),rand.nextInt(n));

    }


    @Override
    public double getFitness(double[] pos) {
        return pathLen(posToPath(pos));
    }

    public int[] posToPath(double[] pos) {
        TownPair[] pairs = posToTownPairs(pos);
        Arrays.sort(pairs, (TownPair a, TownPair b)-> Double.compare(a.x,b.x) );
        int[] path = extractTowns(pairs);
        the2opt(path,5);
        return path;
    }
    
    private static class TownPair {
        public int town;
        public double x;
        public TownPair(int town, double x) {
            this.town = town;
            this.x = x;
        }
    }
    
    private TownPair[] posToTownPairs(double[] pos){
        TownPair[] ret = new TownPair[n];
        for (int i = 0; i < n; i++) {ret[i] = new TownPair(i,pos[i]);}
        return ret;
    }

    private int[] extractTowns(TownPair[] pairs) {
        int[] ret = new int[n];
        for (int i = 0; i < n; i++) {ret[i] = pairs[i].town;}
        return ret;
    }





    private double get(int i, int j) {
        if (i<j) { return matrix[i][j]; }
        else     { return matrix[j][i]; }
    }


    @Override
    public void run(int numIterations, Consumer<TSP.IterationInfo> logFun) {
        pso.init();
        for (int i = 0; i < numIterations; i++) {
            int[] path = doOneIteration();

            psoView1.draw();
            psoView2.draw();

            String msg = "["+ i +"]";
            logFun.accept( new TSP.IterationInfo(path ,msg, i) );
        }
    }

    @Override
    public int[] doOneIteration() {
        pso.doOneIteration();
        return posToPath(pso.getBest());
    }

    @Override
    public int[] findPath() {
        return posToPath( pso.initParticle().getPos() );
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
            sum += get(path[i % n], path[(i + 1) % n]);
        }
        return sum;
    }

    @Override
    public TSP.EdgeInfo[] getEdgeInfos() {
        return new TSP.EdgeInfo[0];
    }



    // todo : Následuje 2opt -- sprostě zkopírováno, nutno DRY !!!

    private void the2opt(int[] path, int maxTries) {

        int tries = 0;
        boolean modified = true;

        while (modified && tries < maxTries) {

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

        double distNow = get( from1, to1   ) + get( from2, to2 );
        double distAlt = get( from1, from2 ) + get( to1  , to2 );

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


}
