package X.Nodes;

import X.Lexer.Position;

public class ForStmt extends Stmt {

    public Stmt S1;
    public Expr E2;
    public Stmt S3;
    public Stmt S;


    public ForStmt(Stmt s1AST, Expr e2AST, Stmt s3AST, Stmt sAST, Position pos) {
        super(pos);
        S1 = s1AST;
        E2 = e2AST;
        S3 = s3AST;
        S = sAST;
        S1.parent = E2.parent = S3.parent = S.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitForStmt(this, o);
    }

}