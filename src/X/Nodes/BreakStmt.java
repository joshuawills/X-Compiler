package X.Nodes;

import X.Lexer.Position;

public class BreakStmt extends Stmt {

    public BreakStmt(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitBreakStmt(this, o);
    }
}


