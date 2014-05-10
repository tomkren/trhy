package cz.tomkren.trhy.stuff;

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

    public static class Arrow extends Commodity {
        private Commodity inputComo;
        private Commodity outputComo;

        public String getName() {
            return "( "+ inputComo.getName() +" -> "+ outputComo.getName() +" )";
        }

        public Arrow(Commodity inputComo, Commodity outputComo) {
            this.inputComo = inputComo;
            this.outputComo = outputComo;
        }
        public Commodity getInputComo() {
            return inputComo;
        }
        public Commodity getOutputComo() {
            return outputComo;
        }
    }
    
}
