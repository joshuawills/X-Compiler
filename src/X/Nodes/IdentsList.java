package X.Nodes;

import X.Lexer.Position;

public class IdentsList extends List {

    public Ident I;
    public List IL;

    public Type thisT; // Used to store the type for ease of use in the emitter
    public String indexT; // Used to store the index in the same way local vars do etc

    public IdentsList(Ident i, List il, Position pos) {
        super(pos);
        I = i;
        IL = il;
        I.parent = IL.parent = this;
    }
    
    public Object visit(Visitor v, Object o) {
        return v.visitIdentsList(this, o);
    }
}
