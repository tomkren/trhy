package cz.tomkren.zoo;


import cz.tomkren.trhy.helpers.Log;
import cz.tomkren.trhy.helpers.Utils;
import java.util.Random;

public class PSO {

    public static void main(String[] args) {
        Log.it("-- PSO main --");

        PSO.Opts psoOpts = new PSO.Opts(
                pos -> pos[0]*pos[0] + pos[1]*pos[1], // fitness
                10, // numParticles
                2,  // n (dim)
                new double[]{-5.12,-5.12}, // mins
                new double[]{ 5.12, 5.12}, // maxs
                0.9, // omega
                1,   // phi_p
                1,   // phi_g
                2.5,  // v_max
                0    // phi_rand
        );

        PSO pso = new PSO(psoOpts);

        new PSOView(pso, 0, 1);

    }

    public static interface Fitness {
        public double getFitness(double[] pos);
    }

    public static double[] mkArr(int len, double val) {
        double[] arr = new double[len];
        for (int i = 0; i < len; i++) {
            arr[i] = val;
        }
        return arr;
    }

    public static class Opts {

        private Fitness fitness;
        private int numParticles;
        private int n;
        private double[] mins;
        private double[] maxs;
        private double omega;
        private double phi_p;
        private double phi_g;
        private double phi_rand;
        private double v_max;
        public Opts(Fitness fitness, int numParticles, int n, double[] mins, double[] maxs, double omega, double phi_p, double phi_g, double v_max, double phi_rand) {
            this.fitness = fitness;
            this.numParticles = numParticles;
            this.n = n;
            this.mins = mins;
            this.maxs = maxs;
            this.omega = omega;
            this.phi_p = phi_p;
            this.phi_g = phi_g;
            this.v_max = v_max;
            this.phi_rand = phi_rand;
        }
        public Fitness getFitness() {
            return fitness;
        }
        public int getNumParticles() {
            return numParticles;
        }
        public int getN() {
            return n;
        }
        public double[] getMins() {
            return mins;
        }
        public double[] getMaxs() {
            return maxs;
        }
        public double getOmega() {
            return omega;
        }
        public double getPhi_p() {
            return phi_p;
        }
        public double getPhi_g() {
            return phi_g;
        }
        public double getV_max() {
            return v_max;
        }
        public double getPhi_rand() {
            return phi_rand;
        }
    }

    public static class Particle {

        private double[] pos;
        private double[] v;
        private double[] bestPos;
        private double bestFitVal;
        public Particle(double[] pos, double[] v, double[] bestPos, double bestFitVal) {
            this.pos = pos;
            this.v = v;
            this.bestPos = bestPos;
            this.bestFitVal = bestFitVal;
        }

        public double[] getPos() {
            return pos;
        }
        public double[] getV() {
            return v;
        }
        public double[] getBestPos() {
            return bestPos;
        }
        public double getBestFitVal() {
            return bestFitVal;
        }

        public void setBestFitVal(double bestFitness) {
            this.bestFitVal = bestFitness;
        }

        public void setBestPos(double[] bestPos) {
            this.bestPos = bestPos;
        }

    }


    private Fitness  fitness;

    private Particle[] particles;
    private double[]   best_g;
    private double globalBestFitVal;

    private Random rand;

    private final int numParticles;
    private int n;
    private double[] mins;
    private double[] maxs;
    private double   omega;
    private double   phi_p;
    private double   phi_g;
    private double   v_max;
    private double   phi_rand;



    public PSO(Opts opts) {

        rand = new Random();

        fitness = opts.getFitness();
        numParticles = opts.getNumParticles();
        n = opts.getN();
        mins = opts.getMins();
        maxs = opts.getMaxs();
        omega = opts.getOmega();
        phi_p = opts.getPhi_p();
        phi_g = opts.getPhi_g();
        v_max = opts.getV_max();
        phi_rand = opts.getPhi_rand();

        init();

    }

    public void init() {
        best_g = new double[n];
        globalBestFitVal = Double.POSITIVE_INFINITY;

        particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++) {
            Particle newParticle = initParticle();
            particles[i] = newParticle;

            double fitVal = newParticle.getBestFitVal();
            if (fitVal < globalBestFitVal) {
                globalBestFitVal = fitVal;
                best_g = newParticle.getPos().clone();
            }
        }
    }

    public Particle initParticle() {
        double[] pos = new double[n];
        double[] v   = new double[n];
        for (int i = 0; i < n; i++) {

            pos[i] = Utils.U(rand, mins[i], maxs[i]);
            v[i]   = Utils.U(rand, -v_max , v_max  );
        }

        checkAndAdjustV(v);

        double fitVal = fitness.getFitness(pos);

        return new Particle(pos, v, pos.clone(), fitVal);
    }


    public double[] getBest() {
        return best_g;
    }

    public Particle[] getParticles() {
        return particles;
    }
    public double[] getMins() {
        return mins;
    }
    public double[] getMaxs() {
        return maxs;
    }

    public void doOneIteration() {

        for (Particle p : particles) {

            // update
            // todo zahrnout reakci na vypadnutí z čtverce
            double r_p = rand.nextDouble();
            double r_g = rand.nextDouble();
            double[] pos    = p.getPos();
            double[] v      = p.getV();
            double[] best_p = p.getBestPos();

            for (int i = 0; i < n; i++) {

                pos[i] += v[i]; // není std (ale o chlup), ale přijde mi lepší
                                // při kreslení (šipky víc odpovídají tomu kam se to pohne)
                                // std varianta to dela v opačném pořadí, nejdřív update v pak až pos

                v[i] = omega * v[i] + phi_p*r_p*(best_p[i]-pos[i])
                                    + phi_g *r_g*(best_g[i]-pos[i])
                                    + phi_rand * rand.nextDouble();
            }

            checkAndAdjustV(v);

            double newFitVal = fitness.getFitness(pos);

            if (newFitVal < p.getBestFitVal()) {
                p.setBestFitVal(newFitVal);
                p.setBestPos(pos.clone());
            }

            if (newFitVal < globalBestFitVal) {
                globalBestFitVal = newFitVal;
                best_g = pos.clone();
            }

        }

        Log.it("best fitness: "+globalBestFitVal);

    }


    private void checkAndAdjustV(double[] v) {
        double sum2 = 0;
        for (int i = 0; i < n; i++) {
            sum2 += v[i]*v[i];
        }
        double size = Math.sqrt(sum2);

        if (size > v_max) {
            double alpha = v_max/size;
            for (int i = 0; i < n; i++) {
                v[i] *= alpha;
            }
        }
    }









}
