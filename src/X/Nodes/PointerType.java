package X.Nodes;

import X.Lexer.Position;

public class PointerType extends Type {

    public Type t;

    public PointerType(Position pos, Type type) {
        super(pos);
        t = type;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitPointerType(this, o);
    }

    @Override
    public String toString() {
        return t.toString() + " *";
    }

    public boolean equals (Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        }
        if (obj instanceof PointerType) {
            return ((PointerType) obj).t != null && ((PointerType) obj).t.equals(t);
        }
        return false;
    }

    public boolean assignable(Object obj) {
        return equals(obj);
    }

}
