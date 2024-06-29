package X.Nodes;

import X.Lexer.Position;

public class MathDeclStmt extends Stmt {

    public Ident I;
    public Expr E;
    public Operator O;

    public MathDeclStmt(Ident iAST, Expr eAST, Operator oAST, Position p) {
        super(p);
        I = iAST;
        E = eAST;
        O = oAST;
        I.parent = E.parent = O.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMathDeclStmt(this, o);
    }

}
