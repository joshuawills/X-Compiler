package X.Nodes;

import X.Lexer.Position;

public class StringType extends Type {

    public StringType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStringType(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof StringType;
        }
    }

    @Override
    public String toString() {
        return "string";
    }


    public boolean assignable(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof StringType;
        }
    }
}
