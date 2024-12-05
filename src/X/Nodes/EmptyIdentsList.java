package X.Nodes;

import X.Lexer.Position;

public class EmptyIdentsList extends List {

    public EmptyIdentsList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyIdentsList(this, o);
    }
    
}
