package X.Nodes;

import X.Lexer.Position;

public class Enum extends Decl {

    public String[] IDs;
    public Ident I;

    public Enum(String[] idAST, Ident iAST, Position pos) {
        super(pos, false);
        I = iAST;
        IDs = idAST;
        I.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEnum(this, o);
    }

}
