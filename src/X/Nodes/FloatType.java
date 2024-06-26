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
    public boolean assignable(Object obj) {
        return equals(obj);
    }
}
