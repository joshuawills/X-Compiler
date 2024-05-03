package X.Nodes;

import X.Lexer.Position;

public class StmtList extends List {

    public Object S; // can be a stmt or a LocalVar
    public List SL;

    public StmtList(Stmt sAST, List slAST, Position pos) {
        super(pos);
        S = sAST;
        SL = slAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStmtList(this, o);
    }
}
