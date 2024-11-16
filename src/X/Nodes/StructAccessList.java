package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class StructAccessList extends List {

    public Ident SA;
    public List SAL;
    public Struct ref;
    public Optional<Expr> arrayIndex;

    public StructAccessList(Ident saAST, List salAST, Position pos, Optional<Expr> arrayIndexAST) {
        super(pos);
        SA = saAST;
        SAL = salAST;
        arrayIndex = arrayIndexAST;
        SA.parent = SAL.parent = this;
        if (arrayIndex.isPresent()) {
            arrayIndex.get().parent = this;
        }
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructAccessList(this, o);
    }
}