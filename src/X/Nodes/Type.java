package X.Nodes;

import X.Lexer.Position;

public abstract class Type extends AST {

    public Type(Position pos) {
        super(pos);
    }

    public abstract boolean equals(Object obj);
    public abstract String toString();
    public abstract String getMini();

    public abstract boolean assignable(Object obj);

    public boolean isVoid() {
        return (this instanceof VoidType);
    }

    public boolean isNumeric() {
        return (this instanceof I8Type || this instanceof I32Type || this instanceof I64Type || this instanceof F32Type
         || this instanceof F64Type);
    }

    public boolean isInteger() {
        return (this instanceof I8Type || this instanceof I32Type || this instanceof I64Type);
    }

    public boolean isFloat() {
        return (this instanceof F32Type || this instanceof F64Type);
    }

    public boolean isI8() {
        return this instanceof I8Type;
    }

    public boolean isI32() {
        return this instanceof I32Type;
    }

    public boolean isI64() {
        return this instanceof I64Type || this instanceof EnumType;
    } 

    public boolean isF32() {
        return (this instanceof F32Type);
    }

    public boolean isF64() {
        return (this instanceof F64Type);
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

    public boolean isPointerToStruct() {
        return this instanceof PointerType && ((PointerType) this).t instanceof StructType;
    }

    public boolean isVoidPointer() {
        return this instanceof PointerType && ((PointerType) this).t.isVoid();
    }

    public boolean isMurkyPointer() {
        return this instanceof PointerType && ((PointerType) this).t.isMurky();
    }

    public boolean isArray() {
        return this instanceof ArrayType;
    }

    public boolean isMurkyArray() {
        return this instanceof ArrayType && ((ArrayType) this).t.isMurky();
    }

    public boolean isMurky() {
        return this instanceof MurkyType;
    }

    public boolean isEnum() {
        return this instanceof EnumType;
    }

    public boolean isUnknown() {
        return this instanceof UnknownType;
    }

    public boolean isStruct() {
        return this instanceof StructType;
    }

}
