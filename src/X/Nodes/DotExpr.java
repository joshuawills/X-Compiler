package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class DotExpr extends Expr {

    public Ident I;
    public Optional<Expr> arrayIndex;
    public Expr E;
    public boolean isPointerAccess;

    public DotExpr(Ident iAST, Expr eAST, Position pos, Optional<Expr> arrayAST, boolean isPointerAST) {
        super(pos);
        I = iAST;
        E = eAST;
        arrayIndex = arrayAST;
        isPointerAccess = isPointerAST;
        I.parent = E.parent = this;
        if (arrayIndex.isPresent()) {
            arrayIndex.get().parent = this;
        }
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDotExpr(this, o);
    }

}