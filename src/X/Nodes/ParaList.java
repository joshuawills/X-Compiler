package X.Nodes;

import X.Lexer.Position;

public class ParaList extends List {

    public ParaDecl P;
    public List PL;

    public ParaList(ParaDecl pAST, List plAST, Position pos) {
        super(pos);
        P = pAST;
        PL = plAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitParaList(this, o);
    }

}
