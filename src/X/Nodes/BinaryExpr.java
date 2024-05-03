package X.Nodes;

import X.Lexer.Position;

public class BinaryExpr extends Expr {

    public Expr E1, E2;
    public Operator O;

    public BinaryExpr(Expr e1AST, Expr e2AST, Operator oAST, Position pos) {
        super(pos);
        E1 = e1AST;
        E2 = e2AST;
        O = oAST;
        O.parent = E1.parent = E2.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitBinaryExpr(this, o);
    }

}
