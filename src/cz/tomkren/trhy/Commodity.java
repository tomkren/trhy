package cz.tomkren.trhy;

public abstract class Commodity {

    public abstract String getName();

    @Override
    public String toString() {return getName();}

    @Override
    public boolean equals(Object o) {
        if (o instanceof Commodity) {
            return getName().equals( ((Commodity) o).getName() );
        }
        return false;
    }
    
    public static class Basic extends Commodity {
        private String name;
        public Basic(String name) {this.name = name;}
        public String getName() {return name;}
    }
    
}
