package X.Nodes;

import X.Lexer.Position;

public class TraitFunction extends Decl {
    
    public List PL;
    
    public TraitFunction(Type tAST, Ident idAST, List plAST, Position pos) {
        super(pos, false);
        T = tAST;
        I = idAST;
        PL = plAST;
        T.parent = I.parent = PL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTraitFunction(this, o);
    }

}
