package X.Nodes;

import X.Lexer.Position;

public class ContinueStmt extends Stmt {

    public ContinueStmt(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitContinueStmt(this, o);
    }
}


