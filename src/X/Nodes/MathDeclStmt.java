package X.Nodes;

import X.Lexer.Position;

public class MathDeclStmt extends Stmt {

    public Ident I;
    public Expr E;
    public Operator O;
    public boolean isDeref = false;

    public MathDeclStmt(Ident iAST, Expr eAST, Operator oAST, Position p, boolean iD) {
        super(p);
        I = iAST;
        E = eAST;
        O = oAST;
        isDeref = iD;
        I.parent = E.parent = O.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMathDeclStmt(this, o);
    }

}
