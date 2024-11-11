package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class StructAccess extends Expr {

    public Struct ref; // this is the reference struct
    public Ident varName;
    public StructAccessList L;
    public Optional<Expr> arrayIndex;
    public Type sourceType;

    public StructAccess(Struct refAST, Ident varNameAST, StructAccessList lAST, Position pos, Optional<Expr> arrayIndexAST, Type sourceTypeAST) {
        super(pos);
        ref = refAST;
        varName = varNameAST;
        L = lAST;
        arrayIndex = arrayIndexAST;
        sourceType = sourceTypeAST;
        sourceType.parent = ref.parent = varName.parent = L.parent = this;
        if (arrayIndex.isPresent()) {
            arrayIndex.get().parent = this;
        }
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitStructAccess(this, o);
    }
}
