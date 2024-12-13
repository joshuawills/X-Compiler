package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class DotExpr extends Expr {

    public Ident I;
    public Optional<Expr> arrayIndex;
    public Expr E;
    public boolean isPointerAccess;
    public Optional<List> args;

    public DotExpr(Ident iAST, Expr eAST, Position pos, Optional<Expr> arrayAST, boolean isPointerAST, Optional<List> argsAST) {
        super(pos);
        I = iAST;
        E = eAST;
        arrayIndex = arrayAST;
        args = argsAST;
        isPointerAccess = isPointerAST;
        I.parent = E.parent = this;
        if (arrayIndex.isPresent()) {
            arrayIndex.get().parent = this;
        }
        if (args.isPresent()) {
            args.get().parent = this;
        }
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDotExpr(this, o);
    }

}