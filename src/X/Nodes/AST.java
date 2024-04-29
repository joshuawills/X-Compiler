package X.Nodes;

import X.Lexer.Position;

public abstract class AST {

    public Position pos;
    public AST parent;

    public AST(Position pos) {
        this.pos = pos;
    }

    public Position getPosition() {
        return this.pos;
    }

    public abstract Object visit(Visitor v, Object o);

}
