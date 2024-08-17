package X.Nodes;

import X.Lexer.Position;

public class AssignmentExpr extends Expr {

    public Expr LHS;
    public Operator O; // will always be an assignment operator
    public Expr RHS;

    public AssignmentExpr(Expr lhsAST, Operator oAST, Expr rhsAST, Position pos) {
        super(pos);
        LHS = lhsAST;
        O = oAST;
        RHS = rhsAST;
        LHS.parent = O.parent = RHS.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitAssignmentExpr(this, o);
    }

}