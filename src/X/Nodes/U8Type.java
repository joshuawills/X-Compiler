package X.Nodes;

import X.Lexer.Position;

public class U8Type extends Type {

    public U8Type(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitU8Type(this, o);
    }

    public boolean equals(Object obj) {
        return obj instanceof ErrorType || obj instanceof U8Type;
    }

    public String toString() {
        return "u8";
    }

    public String getMini() {
        return "U8";
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
