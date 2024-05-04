package X.Nodes;

import X.Lexer.Position;

public class EmptyCompStmt extends Stmt {

    public EmptyCompStmt(Position pos) {
        super(pos);
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitEmptyCompStmt(this, o);
    }
}
