package X.Nodes;

import X.Lexer.Position;

public abstract class Expr extends AST {

    public Type type;
    public int tempIndex;
    public boolean isLHSOfAssignment;

    public Expr(Position pos) {
        super(pos);
        type = null;
    }

    public boolean isEmptyExpr() {
        return this instanceof EmptyExpr;
    }

    public boolean isArrayIndexExpr() {
        return this instanceof ArrayIndexExpr;
    }

    public boolean isStructExpr() {
        return this instanceof StructExpr;
    }

    public boolean isStringExpr() {
        return this instanceof StringExpr;
    }

    public boolean isVarExpr() {
        return this instanceof VarExpr;
    }

    public boolean isDotExpr() {
        return this instanceof DotExpr;
    }

}
