package cz.tomkren.trhy.stuff;

import cz.tomkren.fishtron.Type;

public interface Machine {

    public Stuff      work (Stuff input);
    public boolean    checkInput (Stuff input);
    public String     getMachineID();
    public Type.Arrow getType();

}
