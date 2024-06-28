package X.Nodes;

import X.Lexer.Position;

public class DoWhileStmt extends Stmt {

    public Expr E;
    public Stmt S;

    public DoWhileStmt(Expr eAST, Stmt sAST, Position pos) {
        super(pos);
        E = eAST;
        S = sAST;
        E.parent = S.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDoWhileStmt(this, o);
    }

}
