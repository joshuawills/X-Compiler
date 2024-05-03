package X.Nodes;

import X.Lexer.Position;

public class ReturnStmt extends Stmt {

    public Expr E;

    public ReturnStmt(Expr eAST, Position pos) {
        super(pos);
        E = eAST;
        E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitReturnStmt(this, o);
    }
}
