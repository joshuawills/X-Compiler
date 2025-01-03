package X.Nodes;

import X.Lexer.Position;

public class ArrayType extends Type {

    public Type t;
    public int length; // set to -1 if unknown

    public ArrayType(Position pos, Type type, int l) {
        super(pos);
        t = type;
        length = l;
    }

    @Override
    public String toString() {
        return t.toString() + "[]";
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        }
        if (obj instanceof ArrayType) {
            return ((ArrayType) obj).t.equals(t);
        }
        return false;
    }

    public boolean assignable(Object obj) {
        if (obj instanceof PointerType) {
            return ((PointerType) obj).t.equals(t);
        }
        return equals(obj);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitArrayType(this, o);
    }

    public String getMini() {
        return "P" + t.getMini();
    }
}
