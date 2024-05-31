package X.Nodes;

import X.Lexer.Position;

public class StmtList extends List {

    public Stmt S;
    public List SL;

    public StmtList(Stmt sAST, List slAST, Position pos) {
        super(pos);
        S = sAST;
        SL = slAST;
        S.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStmtList(this, o);
    }
}
