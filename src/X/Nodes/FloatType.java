package X.Nodes;

import X.Lexer.Position;

public class FloatType extends Type {

    public FloatType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitFloatType(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof FloatType;
        }
    }

    @Override
    public String toString() {
        return "float";
    }

    public String getMini() {
        return "F";
    }

    public boolean assignable(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        }
        return obj instanceof IntType || obj instanceof FloatType || obj instanceof CharType;
    }
}
