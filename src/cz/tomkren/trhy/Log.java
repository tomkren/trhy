package cz.tomkren.trhy;

/**
 * Created by sekol on 29.3.2014.
 */
public class Log {

    private static Log log = new Log();

    public static Log it (Object o) {
        System.out.println( o );
        return log;
    }

    public static Log it () {
        System.out.println();
        return log;
    }

}
