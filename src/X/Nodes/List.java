package X.Nodes;

import X.Lexer.Position;

public abstract class List extends AST {

    public boolean containsExit = false;
    public List(Position pos) {
        super(pos);
    }

}
