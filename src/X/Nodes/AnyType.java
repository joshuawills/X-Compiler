package X.Nodes;

import X.Lexer.Position;

public class AnyType extends Type {
    
    public AnyType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitAnyType(this, o);
    }

    public boolean equals(Object obj) {
        return true;
    }

    @Override
    public String toString() {
        return "any";
    }

    public String getMini() {
        return "A";
    }

    public boolean assignable(Object obj) {
        return true;
    }
}
