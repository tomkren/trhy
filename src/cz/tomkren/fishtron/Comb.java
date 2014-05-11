package cz.tomkren.fishtron;
import cz.tomkren.trhy.helpers.Log;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Comb {
    public Comb ap(Comb comb);

    default public Comb ap (int i) {
        return ap(new Val<Integer>(i));
    }

    public static final Comb plus = new Fun2<Integer,Integer,Integer>("plus", (x,y)-> x+y);
    public static final Comb succ = new Fun<Integer,Integer>("succ", x -> x+1);
    public static final Comb _0   = new Val<>(0);
    public static final Comb _1   = new Val<>(1);
    public static final Comb _2   = new Val<>(2);
    public static final Comb K_   = x -> (y -> x);
    public static final Comb K    = mk("K", x -> (y -> x) );
    public static final Comb S    = mk("S", f -> (g -> (x -> f.ap(x).ap(g.ap(x)) )));

    public static Comb mk(String str, Comb f){
        return new Comb() {
            public Comb ap(Comb x) {
                Comb result = f.ap(x);
                if (result instanceof Val) {
                    return result;
                } else {
                    return mk("("+str+" "+x.toString()+")", result);
                }
            }
            public String toString() {
                return str;
            }
        };
    }

    public class Fun<A,B> implements Comb {
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
        public Comb ap(Comb x) {
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

    public class Fun2<A,B,C> implements Comb {
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
        public Comb ap(Comb x) {
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

    public class Val<T> implements Comb {
        private T x;
        public Val(T x) {
            this.x = x;
        }
        public T get() {
            return x;
        }
        public Comb ap(Comb comb) {
            throw new UnsupportedOperationException("Val<T> cannot by applied.");
        }
        @Override public String toString() {
            return x.toString();
        }
    }

    public static void main(String[] args) {
        Log.it("Hello Combinatorz!");
        Log.it( _0 );
        Log.it( succ.ap(0) );
        Log.it( plus.ap(2).ap(2) );
        Log.it( plus.ap(1) );
        Log.it( succ );
        Log.it( plus );
        Log.it( succ.ap(plus.ap(2).ap(2)) );

        Log.it().it( "K_:"              );
        Log.it( K_.ap(2).ap(1)            );
        Log.it( K_.ap(2)                 );
        Log.it( K_.ap(K_).ap(1).ap(2).ap(3) );

        Log.it().it( "K:"              );
        Log.it( K.ap(2).ap(1)            );
        Log.it( K.ap(2)                 );
        Log.it( K.ap(K).ap(1).ap(2).ap(3)  );

        Log.it().it("S:");
        Log.it( "S K K    = "+ S.ap(K).ap(K)       );
        Log.it( "S K K 42 = "+ S.ap(K).ap(K).ap(42) );

    }
}
