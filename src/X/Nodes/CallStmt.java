package X.Nodes;

import X.Lexer.Position;

public class CallStmt extends Stmt {
    public CallExpr E;

    public CallStmt(CallExpr e, Position pos) {
        super(pos);
        E = e;
        E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitCallStmt(this, o);
    }
}
