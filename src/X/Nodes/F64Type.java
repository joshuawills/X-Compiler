package X.Nodes;

import X.Lexer.Position;

public class F64Type extends Type {

    public F64Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitF64Type(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof F64Type;
        }
    }

    @Override
    public String toString() {
        return "f64";
    }

    public String getMini() {
        return "F64";
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
