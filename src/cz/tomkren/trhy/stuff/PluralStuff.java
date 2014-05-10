package cz.tomkren.trhy.stuff;

/* Slouží pro komodity jejichž předměty jsou vzájemně nerozeznatelné,
   a tak je možno jejich množství rozlišit číslem (které je navíc reálné). */
public interface PluralStuff extends Stuff {

    public double getNum ();
    public double addNum (double delta);

    default String dumpKey() {
        return getComoName();
    }
    default double dumpVal() {
        return getNum();
    }

}
