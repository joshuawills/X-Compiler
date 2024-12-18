package X.Nodes;

import X.Lexer.Position;

public class Trait extends Decl {

    public List TL; // Traits list
    public Ident name;

    public Trait(List tlAST, Ident nameAST, Position pos) {
        super(pos, false);
        TL = tlAST;
        name = nameAST;
        TL.parent = name.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTrait(this, o);
    }
    
}
