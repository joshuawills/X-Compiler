package X.Nodes;

import X.Lexer.Position;

public class CompoundStmt extends Stmt {

    public List SL;

    public CompoundStmt(List stmtL, Position pos) {
        super(pos);
        SL = stmtL;
        SL.parent = this;
    }


    public Object visit(Visitor v, Object o) {
        return v.visitCompoundStmt(this, o);
    }

}
