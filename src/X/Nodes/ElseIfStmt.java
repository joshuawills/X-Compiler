package X.Nodes;

import X.Lexer.Position;

public class ElseIfStmt extends Stmt {

    public Expr E; // the condition itself

    public Stmt S1; // The body of the else-if condition

    public Stmt S2; // Potential chaining else if, or empty stmt

    public ElseIfStmt(Expr eAST, Stmt s1AST, Stmt s2AST, Position pos) {
        super(pos);
        E = eAST;
        S1 = s1AST;
        S2 = s2AST;
        E.parent = S1.parent = S2.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitElseIfStmt(this, o);
    }

}
