package X.Nodes;

import X.Lexer.Position;

public class IfStmt extends Stmt {

    public Expr E; // the condition itself

    public Stmt S1; // The body of the if condition

    public Stmt S2; // else-if condition, or empty expr

    public Stmt S3; // else condition, or empty expr

    public IfStmt(Expr eAST, Stmt s1AST, Stmt s2AST, Stmt s3AST, Position pos) {
        super(pos);
        E = eAST;
        S1 = s1AST;
        S2 = s2AST;
        S3 = s3AST;
        E.parent = S1.parent = S2.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIfStmt(this, o);
    }

}
