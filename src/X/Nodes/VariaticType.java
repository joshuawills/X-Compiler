package X.Nodes;

import X.Lexer.Position;

public class VariaticType extends Type {

    public VariaticType(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitVariaticType(this, o);
    }

    public String toString() {
        return "...";
    }

    public boolean equals(Object obj) {
        return true;
    }

    public boolean assignable(Object obj) {
        return true;
    }

    public String getMini() {
        return ".VAR.";
    }
    
}
