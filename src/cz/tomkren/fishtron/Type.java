package cz.tomkren.fishtron;


public interface Type {

    public class Const implements Type {
        private String sym;
        public Const(String sym) {
            this.sym = sym;
        }
        @Override public String toString() {
            return sym;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Const aConst = (Const) o;

            if (!sym.equals(aConst.sym)) return false;

            return true;
        }
        @Override public int hashCode() {
            return sym.hashCode();
        }
    }

    public class Arrow implements Type {
        private Type input;
        private Type output;
        public Arrow(Type input, Type output) {
            this.input = input;
            this.output = output;
        }
        public Arrow(String constSym1, String constSym2) {
            this(new Const(constSym1), new Const(constSym2));
        }
        public Type getInput() {
            return input;
        }
        public Type getOutput() {
            return output;
        }
        @Override public String toString() {
            return "( "+ input +" -> "+ output +" )";
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Arrow arrow = (Arrow) o;

            if (!input.equals(arrow.input)) return false;
            if (!output.equals(arrow.output)) return false;

            return true;
        }
        @Override public int hashCode() {
            int result = input.hashCode();
            result = 31 * result + output.hashCode();
            return result;
        }
    }

/* todo zatim zakomentováno aby se to nepletlo při refactoru
    public class Var implements Type {
        private String sym;
        public Var(String sym) {
            this.sym = sym;
        }
        @Override public String toString() {
            return sym;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Var var = (Var) o;

            if (!sym.equals(var.sym)) return false;

            return true;
        }
        @Override public int hashCode() {
            return sym.hashCode();
        }
    }
*/

}
