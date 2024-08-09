package X.Nodes;

import X.Lexer.Position;

public abstract class Type extends AST {

    public Type(Position pos) {
        super(pos);
    }

    public abstract boolean equals(Object obj);
    public abstract String toString();

    public abstract boolean assignable(Object obj);

    public boolean isVoid() {
        return (this instanceof VoidType);
    }

    public boolean isInt() {
        return (this instanceof IntType || this instanceof CharType);
    }

    public boolean isFloat() {
        return (this instanceof FloatType);
    }

    public boolean isBoolean() {
        return (this instanceof BooleanType);
    }

    public boolean isError() {
        return (this instanceof ErrorType);
    }

    public boolean isPointer() {
        return this instanceof PointerType;
    }

    public boolean isArray() {
        return this instanceof ArrayType;
    }

    public boolean isChar() {
        return this instanceof CharType;
    }
}
