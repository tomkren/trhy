package cz.tomkren.fishtron;
import cz.tomkren.trhy.helpers.Log;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Combinator {
    public Combinator apply(Combinator comb);

    public static final Combinator plus = new Fun2<Integer,Integer,Integer>("plus", (x,y)-> x+y);
    public static final Combinator succ = new Fun<Integer,Integer>("succ", x -> x+1);
    public static final Combinator _0   = new Val<>(0);
    public static final Combinator _1   = new Val<>(1);
    public static final Combinator _2   = new Val<>(2);


    public class Fun<A,B> implements Combinator {
        private Function<A,B> f;
        private String sym;
        private Supplier<String> toStr;
        public Fun(Function<A, B> f) {
            this.f = f;
        }
        public Fun(String sym, Function<A, B> f) {
            this.f = f;
            this.sym = sym;
        }
        public Fun(Function<A, B> f, Supplier<String> toStr) {
            this.f = f;
            this.toStr = toStr;
        }
        public Combinator apply(Combinator x) {
            return new Val<B>(f.apply(((Val<A>) x).get()));
        }

        @Override public String toString() {
            if (sym != null) {
                return sym;
            } else if (toStr != null) {
                return toStr.get();
            } else {
                return super.toString();
            }
        }
    }

    public class Fun2<A,B,C> implements Combinator {
        private BiFunction<A,B,C> f;
        private String sym;
        private Supplier<String> toStr;
        public Fun2(BiFunction<A,B,C> f) {
            this.f = f;
        }
        public Fun2(String sym, BiFunction<A,B,C> f) {
            this.f = f;
            this.sym = sym;
        }
        public Fun2(BiFunction<A, B, C> f, Supplier<String> toStr) {
            this.f = f;
            this.toStr = toStr;
        }
        public Combinator apply(Combinator x) {
            return new Fun<B,C>( y -> f.apply(((Val<A>) x).get(), y) ,
                                () -> "( "+ sym +" "+ ((Val<A>) x).toString() + " )" );
        }
        @Override public String toString() {
            if (sym != null) {
                return sym;
            } else if (toStr != null) {
                return toStr.get();
            } else {
                return super.toString();
            }
        }
    }

    public class Val<T> implements Combinator {
        private T x;
        public Val(T x) {
            this.x = x;
        }
        public T get() {
            return x;
        }
        public Combinator apply(Combinator comb) {
            throw new UnsupportedOperationException("Val<T> cannot by applied.");
        }
        @Override public String toString() {
            return x.toString();
        }
    }

    public static void main(String[] args) {
        Log.it("Hello Combinatorz!");
        Log.it( _0 );
        Log.it( succ.apply(_0) );
        Log.it( plus.apply(_2).apply(_2) );
        Log.it( plus.apply(_1) );
        Log.it( succ );
        Log.it( plus );
        Log.it( succ.apply( plus.apply(_2).apply(_2) ) );



    }
}
