package X.Nodes;

import X.Lexer.Position;

public class LocalVarStmt extends Stmt {

    public LocalVar V;

    public LocalVarStmt(LocalVar vAST, Position pos) {
        super(pos);
        V = vAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitLocalVarStmt(this, o);
    }
}
