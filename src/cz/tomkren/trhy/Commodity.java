package cz.tomkren.trhy;

public interface Commodity {
    @Override
    public String toString();    
    
    public static class Basic implements Commodity {
        private String name;
        public Basic(String name) {this.name = name;}
        @Override
        public String toString() {return name;}
    }
    
}
