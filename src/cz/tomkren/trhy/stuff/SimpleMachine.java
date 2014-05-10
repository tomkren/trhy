package cz.tomkren.trhy.stuff;


import cz.tomkren.fishtron.Type;

public class SimpleMachine implements Machine, SingularStuff {

    private String     machineID;
    private double     beta;
    private Type.Arrow type;

    public SimpleMachine(String machineID, double beta, Type.Arrow como) {
        this.machineID = machineID;
        this.beta      = beta;
        this.type = como;
    }

    public boolean checkInput(Stuff input) {
        return (input instanceof Quantum) && input.getComo().equals(type.getInput());
    }

    @Override
    public Stuff work(Stuff input) {
        /* todo | ve firmě se už tento check dělá, tzn je tu zbytečný: rozhodnout zda odsud odebrat,
         * todo | zvážit řešení pomocí výjimek, zatím to vypínám. */
        //if (!checkInput(input)) { return new PluralStuffFail("Incorrect input to simpleMachine."); }
        PluralStuff plInput = (PluralStuff) input; // checkInput zajišťuje že ok (Quantum implements PluralStuff)
        return new Quantum(type.getOutput(), beta * plInput.getNum() );
    }

    @Override public Type   getComo() {
        return type;
    }
    @Override public String getSgID() {
        return machineID;
    }
    @Override public String getMachineID() {
        return machineID;
    }
}
