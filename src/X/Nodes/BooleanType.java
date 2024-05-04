package X.Nodes;

import X.Lexer.Position;

public class BooleanType extends Type {

    public BooleanType(Position pos) {
        super(pos);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof BooleanType;
        }
    }

    public boolean assignable(Object obj) {
        return equals(obj);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitBooleanType(this, o);
    }

}


