package X.Nodes;

import X.Lexer.Position;

public class DeclStmt extends Stmt {

    public Ident I;
    public Expr E;
    public boolean isDeref = false;

    public DeclStmt(Ident iAST, Expr eAST, Position pos, boolean iD) {
        super(pos);
        I = iAST;
        E = eAST;
        isDeref = iD;
        I.parent = E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDeclStmt(this, o);
    }

}
