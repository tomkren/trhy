package cz.tomkren.trhy;

public class BasicComodity implements Comodity {

    private String name;
    
    public BasicComodity(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
     
}
