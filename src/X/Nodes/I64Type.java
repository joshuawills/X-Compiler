package X.Nodes;

import X.Lexer.Position;

public class I64Type extends Type {


    public I64Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitI64Type(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof I64Type;
        }
    }

    @Override
    public String toString() {
        return "i64";
    }

    public String getMini() {
        return "I64";
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
