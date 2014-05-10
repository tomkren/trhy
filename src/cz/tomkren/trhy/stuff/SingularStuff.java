package cz.tomkren.trhy.stuff;


public interface SingularStuff extends Stuff {

    public String getSgID();

    default String dumpKey() {
        return getComoName();
        //return getSgID();
    }
    default double dumpVal() {
        return 1;
    }

}
