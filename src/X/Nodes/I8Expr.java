package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class I8Expr extends Expr {

    public Optional<CharLiteral> CL = Optional.empty();
    public Optional<IntLiteral> IL = Optional.empty();

    public I8Expr(CharLiteral clAST, Position pos) {
        super(pos);
        CL = Optional.of(clAST);
        CL.get().parent = this;
    }

    public I8Expr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = Optional.of(ilAST);
        IL.get().parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitCharExpr(this, o);
    }
}
