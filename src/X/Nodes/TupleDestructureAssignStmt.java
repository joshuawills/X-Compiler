package X.Nodes;

import X.Lexer.Position;

public class TupleDestructureAssignStmt extends Stmt {

    public TupleDestructureAssign TDA;

    public TupleDestructureAssignStmt(TupleDestructureAssign tdaAST, Position pos) {
        super(pos);
        TDA = tdaAST;
        TDA.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTupleDestructureAssignStmt(this, o);
    }
    
}
