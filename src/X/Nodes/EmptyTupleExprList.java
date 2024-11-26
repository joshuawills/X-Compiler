package X.Nodes;

import X.Lexer.Position;

public class EmptyTupleExprList extends List {

    public EmptyTupleExprList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyTupleExprList(this, o);
    }
    
}
