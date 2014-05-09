package cz.tomkren.trhy;


public interface Machine {

    public Stuff   work (Stuff input);
    public boolean checkInput (Stuff input);
    public String  getMachineID();

    // todo debilní hax
    public static boolean isMachineName(String str) {
        return str.contains("->");
    }


    // todo zapracovat do systému správně
    public class SimpleMachine implements Machine, Stuff {

        private String machineID;
        private double beta;
        private Commodity inputComo;
        private Commodity outputComo;
        private Commodity commodity;

        public SimpleMachine(String machineID, double beta, Commodity inputComo, Commodity outputComo) {
            this.machineID = machineID;
            this.beta = beta;
            this.inputComo = inputComo;
            this.outputComo = outputComo;

            commodity = new Commodity.Arrow(inputComo, outputComo);
        }

        @Override
        public double getNum() {
            return 1;
        }

        @Override
        public double addNum(double delta) {
            // todo předelat ty interfacey tak, aby se to nemuselo takle blbě
            throw new UnsupportedOperationException("není možno přidávat počty mašin (rozdělaný a bude to udelaný jinač ...)");
        }

        @Override
        public Commodity getComo() {
            return commodity;
        }

        public boolean checkInput(Stuff input) {
            if (input instanceof Stuff.Basic) {
                Stuff.Basic bs = (Stuff.Basic) input;
                return bs.getComo().equals(inputComo);
            }
            return false;
        }

        @Override
        public Stuff work(Stuff input) {

            if (!checkInput(input)) {
                return new Stuff.Fail("Incorrect input to basic machine.");
            }

            return new Stuff.Basic(outputComo, beta * input.getNum() );
        }


        @Override
        public String getMachineID() {
            return machineID;
        }
    }

}
