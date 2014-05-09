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

    // todo zbavit se tohodle (hlavni demence: kaplý s mašinou, netřeba), Arrow je lepší (cheme Comoditu jako ekviv typu)
    /*public static class Mach extends Commodity {
        private Machine machine;
        public Mach(Machine m) {
            machine = m;
        }
        public String getName() {
            return machine.getName();
        }
    }*/

    public static class Arrow extends Commodity {
        private Commodity input;
        private Commodity output;

        public Arrow(Commodity input, Commodity output) {
            this.input = input;
            this.output = output;
        }

        public String getName() {
            return "( "+ input.getName() +" -> "+ output.getName() +" )";
        }
    }
    
}
