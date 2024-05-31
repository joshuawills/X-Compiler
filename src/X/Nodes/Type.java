package X.Nodes;

import X.Lexer.Position;

public abstract class Type extends AST {

    public Type(Position pos) {
        super(pos);
    }

    public abstract boolean equals(Object obj);

    public abstract boolean assignable(Object obj);

    public boolean isVoid() {
        return (this instanceof VoidType);
    }

    public boolean isInt() {
        return (this instanceof IntType);
    }

    public boolean isString() {
        return (this instanceof StringType);
    }

    public boolean isBoolean() {
        return (this instanceof BooleanType);
    }

    public boolean isError() {
        return (this instanceof ErrorType);
    }
}
