package cz.tomkren.trhy;

import java.util.*;

public class Utils {

    // todo debilní hax
    public static boolean isMachineName(String str) {
        return str.contains("->");
    }

    public static final double EPSILON = 0.000000001;    // todo možná ještě líp udělat pomocí relativního epsilon..
    public static boolean isAlmostTheSame(double a, double b) {
        return a == b || Math.abs(a-b) < EPSILON;
    }

    public interface VoidAction {
        public void doIt();
    }

    public static <T> T randSetElem (Random rand, Set<T> set) {
        int i = rand.nextInt( set.size() );
        int j = 0;
        for (T el : set) {
            if (j == i) {
                return el;
            }
            j++;
        }
        return null;
    }
}
