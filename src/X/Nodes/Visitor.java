package X.Nodes;

public interface Visitor {
    
    public abstract Object visitProgram(Program ast, Object o);

    public abstract Object visitIdent(Ident ast, Object o);

}
