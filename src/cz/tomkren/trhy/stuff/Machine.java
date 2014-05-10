package cz.tomkren.trhy.stuff;

public interface Machine {

    public Stuff   work (Stuff input);
    public boolean checkInput (Stuff input);
    public String  getMachineID();

}
