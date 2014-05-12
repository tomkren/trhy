package cz.tomkren.fishtron;


public interface TTerm {

    public Type getType();


    public class Val implements TTerm {
        private String name;
        private Type   type;
        public Val(String name, Type type) {
            this.name = name;
            this.type = type;
        }
        public String getName() {
            return name;
        }
        public Type getType() {
            return type;
        }
    }

    public class Var implements TTerm {
        private String name;
        private Type   type;
        public Var(String name, Type type) {
            this.name = name;
            this.type = type;
        }
        public String getName() {
            return name;
        }
        public Type getType() {
            return type;
        }
    }

    public class App implements TTerm {
        private TTerm m;
        private TTerm n;
        private Type  type;
        public App(TTerm m, TTerm n) {
            this.m = m;
            this.n = n;

            Type mType = m.getType();
            if (!(mType instanceof Type.Arrow)) {
                throw new TTermError("App: m must has a Arrow type.");
            }
            Type.Arrow mArrow = (Type.Arrow) mType;
            if (!mArrow.getInput().equals(n.getType())) {
                throw new TTermError("App: n type is not compatible with m type.");
            }

            type = mArrow.getOutput();
        }
        public TTerm getM() {
            return m;
        }
        public TTerm getN() {
            return n;
        }
        public Type getType() {
            return type;
        }
    }

    public class Lam implements TTerm {
        private String varName;
        private TTerm  body;
        private Type   type;
        public Lam(String varName, Type varType, TTerm body) {
            this.varName = varName;
            this.body = body;
            this.type = new Type.Arrow(varType, body.getType());
        }
        public String getVarName() {
            return varName;
        }
        public TTerm getBody() {
            return body;
        }
        public Type getType() {
            return type;
        }
    }

    public class TTermError extends Error {
        public TTermError(String message) {
            super(message);
        }
    }
}
