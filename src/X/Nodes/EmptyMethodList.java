package X.Nodes;

import X.Lexer.Position;

public class EmptyMethodList extends List {

    public EmptyMethodList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyMethodList(this, o);
    }
    
}
