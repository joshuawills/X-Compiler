package X.Nodes;

import X.Lexer.Position;

public class TraitList extends List {

    public TraitFunction TF;
    public List L;

    public TraitList(TraitFunction tfAST, List lAST, Position pos) {
        super(pos);
        TF = tfAST;
        L = lAST;
        TF.parent = L.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTraitList(this, o);
    }
    
}
