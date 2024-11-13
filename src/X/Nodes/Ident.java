package X.Nodes;

import java.util.Optional;

import X.Lexer.Position;

public class Ident extends Terminal {

    public AST decl;
    public Optional<String> module = Optional.empty();
    public boolean isModuleAccess = false;

    public Ident(String spelling, Position pos) {
        super(spelling, pos);
        decl = null;
    }

    public Ident(String spelling, String module, Position pos) {
        super(spelling, pos);
        this.module = Optional.of(module);
        isModuleAccess = true;
        decl = null;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIdent(this, o);
    }

}
