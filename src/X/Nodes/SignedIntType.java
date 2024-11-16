package X.Nodes;

import X.Lexer.Position;

public class SignedIntType extends Type {

    public String value;

    public SignedIntType(Position pos, String valueAST) {
        super(pos);
        value = valueAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIntType(this, o);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof SignedIntType;
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
