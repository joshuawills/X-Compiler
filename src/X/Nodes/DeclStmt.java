package X.Nodes;

import X.Lexer.Position;

public class DeclStmt extends Stmt{

    public Ident I;
    public Expr E;

    public DeclStmt(Ident iAST, Expr eAST, Position pos) {
        super(pos);
        I = iAST;
        E = eAST;
        I.parent = E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDeclStmt(this, o);
    }

}
