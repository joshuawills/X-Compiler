package X.Nodes;

import X.Lexer.Position;

public class DeclList extends List {

    public Decl D;
    public List DL;

    public DeclList(Decl dAST, List dlAST, Position pos) {
        super(pos);
        D = dAST;
        DL = dlAST;
        D.parent = DL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDeclList(this, o);
    }
}
