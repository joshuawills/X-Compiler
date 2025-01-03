package X.Nodes;

import X.Lexer.Position;

public class ImplementsList extends List {

    public Ident I; // Name of the trait
    public Trait refTrait;

    public List IL;

    public ImplementsList(Ident iAST, List ilAST, Position pos) {
        super(pos);
        I = iAST;
        IL = ilAST;
        I.parent = IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitImplementsList(this, o);
    }
    
}
