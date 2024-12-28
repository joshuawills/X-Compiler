package X.Nodes;

import X.Lexer.Position;

public class ParaList extends List {

    public ParaDecl P;
    public List PL;

    public ParaList(ParaDecl pAST, List plAST, Position pos) {
        super(pos);
        P = pAST;
        PL = plAST;
        P.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitParaList(this, o);
    }

    public boolean validMainParameters() {
        // Must start with an i32 type, then an optional i8**
        if (!P.T.isI32()) {
            return false;
        }
        if (PL.isEmptyParaList()) {
            return true;
        }
        ParaDecl P2 = ((ParaList) PL).P;
        if (!P2.T.isCharPointerPointer()) {
            return false;
        }
        if (!((ParaList) PL).PL.isEmptyParaList()) {
            return false;
        }
        return true;
    }

    public boolean equal(Object o) {
        if (o instanceof ParaList) {
            ParaList pl = (ParaList) o;
            return P.equals(pl.P) && PL.equals(pl.PL);
        }
        return false;
    }
}
