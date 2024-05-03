package X.Nodes;

import X.Lexer.Position;

public class WhileStmt extends Stmt {

    public Expr E;
    public Stmt S;

    public WhileStmt(Expr eAST, Stmt sAST, Position pos) {
        super(pos);
        E = eAST;
        S = sAST;
        E.parent = S.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitWhileStmt(this, o);
    }

}
