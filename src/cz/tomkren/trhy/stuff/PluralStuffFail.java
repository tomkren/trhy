package cz.tomkren.trhy.stuff;

import cz.tomkren.trhy.Commodity;

public class PluralStuffFail implements PluralStuff {
    private String msg;

    public PluralStuffFail(String msg) {
        this.msg = msg;
    }
    @Override
    public Commodity getComo() {
        return null;
    }
    public String getMsg() {
        return msg;
    }

    @Override
    public double getNum() {
        return 0;
    }

    @Override
    public double addNum(double delta) {
        throw new UnsupportedOperationException("Nejde přidávat množství k fail předmětu, to je určitě chyba!");
    }
}
