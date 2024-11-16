package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class SizeOfExpr extends Expr {

    public Optional<VarExpr> varExpr = Optional.empty();
    public Optional<Type> typeV = Optional.empty();


    public Type varType;

    public SizeOfExpr(Optional<VarExpr> varExprAST, Optional<Type> typeAST, Position pos) {
        super(pos);
        varExpr = varExprAST;
        typeV = typeAST;
        if (varExpr.isPresent()) {
            varExpr.get().parent = this;
        }

        if (typeV.isPresent()) {
            typeV.get().parent = this;
        }

        this.type = new I64Type(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitSizeOfExpr(this, o);
    }


    
}
