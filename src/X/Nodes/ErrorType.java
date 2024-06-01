package X.Nodes;

import X.Lexer.Position;

public class ErrorType extends Type {
    public ErrorType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitErrorType(this, o);
    }

    public boolean equals(Object obj) {
        return true;
    }

    @Override
    public String toString() {
        return "error";
    }

    public boolean assignable(Object obj) {
        return true;
    }
}
