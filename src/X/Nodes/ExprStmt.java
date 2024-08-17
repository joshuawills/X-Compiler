package X.Nodes;

import X.Lexer.Position;

public class ExprStmt extends Stmt {

    public Expr E;

    public ExprStmt(Expr eAST, Position pos) {
        super(pos);
        E = eAST;
        E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitExprStmt(this, o);
    }
}
