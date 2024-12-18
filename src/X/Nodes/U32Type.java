package X.Nodes;

import X.Lexer.Position;

public class U32Type extends Type {

    public U32Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitU32Type(this, o);
    }

    public boolean equals(Object obj) {
        return obj instanceof ErrorType || obj instanceof U32Type;
    }

    public String toString() {
        return "u32";
    }

    public String getMini() {
        return "U32";
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
