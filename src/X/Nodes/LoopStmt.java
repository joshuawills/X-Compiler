package X.Nodes;

import java.util.Optional;
import X.Lexer.Position;

public class LoopStmt extends Stmt {

    public Optional<Expr> I1;
    public Optional<Expr> I2;
    public Optional<LocalVar> varName;
    public Stmt S;

    public LoopStmt(Stmt sAST, Optional<Expr> i1AST, Optional<Expr> i2AST, Optional<LocalVar> V, Position pos) {
        super(pos);
        I1 =i1AST;
        I2 = i2AST;
        varName = V;
        S = sAST;
        S.parent = this;
        I1.ifPresent(e -> e.parent = this);
        I2.ifPresent(e -> e.parent = this);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitLoopStmt(this, o);
    }
}
