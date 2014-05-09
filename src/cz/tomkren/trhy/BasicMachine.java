package cz.tomkren.trhy;

public class BasicMachine implements Machine {

    private double beta;
    private Commodity inputComo;
    private Commodity outputComo;

    public BasicMachine(double beta, Commodity inputComo, Commodity outputComo) {
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

        Stuff.Basic bsInput = (Stuff.Basic) input;

        return new Stuff.Basic(outputComo, beta * bsInput.getNum() );
    }
}
