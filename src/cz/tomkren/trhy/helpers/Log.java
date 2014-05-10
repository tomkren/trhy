package cz.tomkren.trhy.helpers;


public class Log {

    private static Log log = new Log();

    public static Log it (Object o, boolean isSilent) {
        if (!isSilent) {
            it(o);
        }
        return log;
    }

    public static Log it (Object o) {
        System.out.println( o );
        return log;
    }

    public static Log it () {
        System.out.println();
        return log;
    }

}
