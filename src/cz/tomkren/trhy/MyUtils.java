package cz.tomkren.trhy;

import java.util.*;

public class MyUtils {

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
