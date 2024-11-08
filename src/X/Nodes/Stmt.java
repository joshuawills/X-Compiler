package X.Nodes;

import X.Lexer.Position;

public abstract class Stmt extends AST {

    public boolean containsExit = false;
    public Stmt(Position pos) {
        super(pos);
    }

    public boolean isReturnStmt() {
        return this instanceof ReturnStmt;
    }

    public boolean isCompoundStmt() {
        return this instanceof CompoundStmt;
    }

}
