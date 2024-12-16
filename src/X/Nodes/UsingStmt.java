package X.Nodes;

import X.Lexer.Position;

public class UsingStmt extends Decl {

    public StringExpr path;
    public Ident ident;

    public boolean isSTLImport = false;

    public UsingStmt(StringExpr path) {
        super(new Position(), false);
        this.path = path;
        path.parent = this;
    }

    public UsingStmt(Ident ident) {
        super(new Position(), false);
        this.ident = ident;
        ident.parent = this;
        isSTLImport = true;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitUsingStmt(this, o);
    }
    
}
