package X.Nodes;

import X.Lexer.Position;

public class MethodList extends List {

    public Method M;
    public List L;

    public MethodList(Method mAST, List lAST, Position pos) {
        super(pos);
        M = mAST;
        L = lAST;
        M.parent = L.parent = this;
    }
    
    public Object visit(Visitor v, Object o) {
        return v.visitMethodList(this, o);
    }
}
