package X.Nodes;

import X.Lexer.Position;

public class I8Type extends Type {

    public I8Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitI8Type(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof I8Type;
        }
    }

    @Override
    public String toString() {
        return "i8";
    }

    public String getMini() {
        return "I8";
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
