package X.Nodes;

import X.Lexer.Position;

public class TraitFunction extends Decl {
    
    public List PL;
    public boolean isPointer = false;
    
    public TraitFunction(Type tAST, Ident idAST, List plAST, Position pos, boolean isMut, boolean isPointerAST) {
        super(pos, isMut);
        isPointer = isPointerAST;
        T = tAST;
        I = idAST;
        PL = plAST;
        T.parent = I.parent = PL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTraitFunction(this, o);
    }

}
