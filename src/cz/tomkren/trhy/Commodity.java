package cz.tomkren.trhy;

public abstract class Commodity {

    public abstract String getName();

    @Override
    public String toString() {return getName();}

    @Override
    public boolean equals(Object o) {
        return o instanceof Commodity && getName().equals(((Commodity) o).getName());
    }
    
    public static class Basic extends Commodity {
        private String name;
        public Basic(String name) {this.name = name;}
        public String getName() {return name;}
    }
    
}
