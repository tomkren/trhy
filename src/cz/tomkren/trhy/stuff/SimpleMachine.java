package cz.tomkren.trhy.stuff;


public class SimpleMachine implements Machine, SingularStuff {

    private String          machineID;
    private double          beta;
    private Commodity.Arrow commodity;

    public SimpleMachine(String machineID, double beta, Commodity inputComo, Commodity outputComo) {
        this.machineID = machineID;
        this.beta      = beta;
        this.commodity = new Commodity.Arrow(inputComo, outputComo);
    }

    public boolean checkInput(Stuff input) {
        return (input instanceof Quantum) && input.getComo().equals(commodity.getInputComo());
    }

    @Override
    public Stuff work(Stuff input) {
        /* todo | ve firmě se už tento check dělá, tzn je tu zbytečný: rozhodnout zda odsud odebrat,
         * todo | možná nejlépe řešit pomocí výjimek */
        if (!checkInput(input)) { return new PluralStuffFail("Incorrect input to simpleMachine."); }
        PluralStuff plInput = (PluralStuff) input; // checkInput zajišťuje že ok (Quantum implements PluralStuff)
        return new Quantum(commodity.getOutputComo(), beta * plInput.getNum() );
    }

    @Override
    public Commodity getComo() {
        return commodity;
    }

    @Override
    public String getSgID() {
        return machineID;
    }

    @Override
    public String getMachineID() {
        return machineID;
    }
}
