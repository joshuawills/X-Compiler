package X.Nodes;

import X.Lexer.Position;

public class UnknownType extends Type {

    public UnknownType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitUnknownType(this, o);
    }

    public String toString() {
        return "unknown";
    }

    public String getMini() {
        return "UNKNOWN";
    }

    public boolean assignable(Object obj) {
        return false;
    }

    public boolean equals(Object obj) {
        return false;
    }
}
