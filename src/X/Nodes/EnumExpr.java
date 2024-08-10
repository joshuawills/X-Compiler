package X.Nodes;

import X.Lexer.Position;

public class EnumExpr extends Expr {

    public Ident Type;
    public Ident Entry;

    public EnumExpr(Ident tAST, Ident eAST, Position pos) {
        super(pos);
        Type = tAST;
        Entry = eAST;
        Type.parent = Entry.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEnumExpr(this, o);
    }

}