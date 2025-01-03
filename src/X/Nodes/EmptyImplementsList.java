package X.Nodes;

import X.Lexer.Position;

public class EmptyImplementsList extends List {

    public EmptyImplementsList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyImplementsList(this, o);
    }
    
}
