package X.Nodes;

import X.Lexer.Position;

public class EmptyTraitList extends List {

    public EmptyTraitList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyTraitList(this, o);
    }
    
}
