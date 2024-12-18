package X.Nodes;

import X.Lexer.Position;

public class U64Type extends Type {

    public U64Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitU64Type(this, o);
    }

    public boolean equals(Object obj) {
        return obj instanceof ErrorType || obj instanceof U64Type;
    }

    public String toString() {
        return "u64";
    }

    public String getMini() {
        return "U64";
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
