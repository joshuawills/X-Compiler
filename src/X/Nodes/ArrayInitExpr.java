package X.Nodes;


import X.Lexer.Position;

public class ArrayInitExpr extends Expr {

    public List AL;

    public ArrayInitExpr(List alAST, Position pos) {
        super(pos);
        AL = alAST;
        AL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitArrayInitExpr(this, o);
    }

}
