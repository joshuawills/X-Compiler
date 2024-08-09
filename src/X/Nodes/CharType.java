package X.Nodes;

import X.Lexer.Position;

public class CharType extends Type {

    public CharType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitCharType(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof CharType;
        }
    }

    @Override
    public String toString() {
        return "char";
    }

    public String getMini() {
        return "C";
    }

    public boolean assignable(Object obj) {
        return equals(obj) || obj instanceof IntType;
    }
}
