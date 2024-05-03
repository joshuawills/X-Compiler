package X.Nodes;

import X.Lexer.Position;

public class EmptyStmt extends Stmt {

    public EmptyStmt(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyStmt(this, o);
    }
}
