package X.Nodes;

import X.Lexer.Position;

public class I32Type extends Type {


    public I32Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitI32Type(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof I32Type;
        }
    }

    @Override
    public String toString() {
        return "i32";
    }

    public String getMini() {
        return "I32";
    }

    public boolean assignable(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        }
        assert(obj instanceof Type);
        Type t = (Type) obj;
        return t.isNumeric();
    }

}
