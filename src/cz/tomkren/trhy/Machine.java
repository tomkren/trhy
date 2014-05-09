package cz.tomkren.trhy;


public interface Machine {

    public Stuff   work (Stuff input);
    public boolean checkInput (Stuff input);
    public String  getName ();



    public class Basic implements Machine {

        private double beta;
        private Commodity inputComo;
        private Commodity outputComo;

        public Basic(double beta, Commodity inputComo, Commodity outputComo) {
            this.beta = beta;
            this.inputComo = inputComo;
            this.outputComo = outputComo;
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
        public String getName() {
            return "( "+inputComo.getName() + " -"+beta+"-> "+ outputComo.getName() +" )";
        }
    }

}
