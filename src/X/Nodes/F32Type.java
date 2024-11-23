package X.Nodes;

import X.Lexer.Position;

public class F32Type extends Type {

    public F32Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitF32Type(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof F32Type;
        }
    }

    @Override
    public String toString() {
        return "f32";
    }

    public String getMini() {
        return "F32";
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
