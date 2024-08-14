package X.Nodes;

import X.Lexer.Position;

public class StructElem extends Decl {

    public StructElem(Type tAST, Ident idAST, Position pos, boolean isMut) {
        super(pos, isMut);
        T = tAST;
        I = idAST;
        T.parent = I.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructElem(this, o);
    }

}
