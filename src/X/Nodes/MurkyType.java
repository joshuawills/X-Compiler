package X.Nodes;

import X.Lexer.Position;

public class MurkyType extends Type {

    public Ident V;

    public MurkyType(Ident vAST, Position pos) {
        super(pos);
        V = vAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMurkyType(this, o);
    }

    public boolean equals(Object obj) {
        return false;
    }

    public boolean assignable(Object obj) {
        return false;
    }

    public String toString() {
        return "murky";
    }

    public String getMini() {
        return "MURKY";
    }
}
