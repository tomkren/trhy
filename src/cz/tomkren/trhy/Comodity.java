package cz.tomkren.trhy;

public interface Comodity {
    @Override
    public String toString();    
    
    public static class Basic implements Comodity {
        private String name;
        public Basic(String name) {this.name = name;}
        @Override
        public String toString() {return name;}
    }
    
}
