package X.Nodes;

import X.Lexer.Position;

public class VoidType extends Type {

    public VoidType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitVoidType(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof VoidType;
        }
    }

    public boolean assignable(Object obj) {
        return equals(obj);
    }

}
