package X.Nodes;

import X.Lexer.Position;

public class ParaDecl extends Decl {

    public ParaDecl(Type tAST, Ident idAST, Position pos, boolean isMut) {
        super(pos, isMut);
        T = tAST;
        I = idAST;
        T.parent = I.parent = this;
    }

    public boolean equals(Object o) {
        if (o instanceof ParaDecl) {
            ParaDecl pd = (ParaDecl) o;
            return T.equals(pd.T) && isMut == pd.isMut;
        } else {
            return false;
        }
    }

    public Object visit(Visitor v, Object o) {
        return v.visitParaDecl(this, o);
    }

}
