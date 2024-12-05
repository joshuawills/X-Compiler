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
            if (t.isVoid()) {
                return true;
            }
            return ((PointerType) obj).t != null && ((PointerType) obj).t.equals(t);
        }
        return false;
    }

    public String getMini() {
        return "P" + t.getMini();
    }

    public boolean assignable(Object obj) {
        assert(obj instanceof Type);
        Type t1 = (Type) obj;

        if (t1.isArray()) {
            Type innerT = ((ArrayType) t1).t;
            return t.equals(innerT);
        }

        if (t1.isVoidPointer()) {
            return true;
        }
        if (t.isVoid() && t1.isPointer()) {
            return true;
        }
        return equals(obj);
    }

}
