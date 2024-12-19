package X.Nodes;

import X.Lexer.Position;

public class Impl extends Decl {

    // List of methods
    public List IL;
    public Ident trait;
    public Ident struct;

    public Impl(List ilAST, Ident traitAST, Ident structAST, Position pos) {
        super(pos, false);
        trait = traitAST;
        struct = structAST;
        IL = ilAST;
        struct.parent = trait.parent = IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitImpl(this, o);
    }
    
}
