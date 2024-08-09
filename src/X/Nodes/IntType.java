package X.Nodes;

import X.Lexer.Position;

public class IntType extends Type {

    public IntType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIntType(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof IntType;
        }
    }

    @Override
    public String toString() {
        return "int";
    }

    public String getMini() {
        return "I";
    }

    public boolean assignable(Object obj) {
        return equals(obj) || obj instanceof CharType;
    }

}
