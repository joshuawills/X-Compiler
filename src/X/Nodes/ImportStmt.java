package X.Nodes;

import X.Lexer.Position;

public class ImportStmt extends Decl {

    public StringExpr path;
    public Ident ident;

    public boolean isSTLImport = false;

    public ImportStmt(StringExpr path, Ident ident) {
        super(new Position(), false);
        this.path = path;
        this.ident = ident;
        path.parent = this;
        ident.parent = this;
    }

    public ImportStmt(Ident ident) {
        super(new Position(), false);
        this.ident = ident;
        isSTLImport = true;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitImportStmt(this, o);
    }

    
}
