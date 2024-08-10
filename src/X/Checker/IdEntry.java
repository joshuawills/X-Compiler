package X.Checker;

import X.Nodes.Decl;
public class IdEntry {

    protected String id;
    protected boolean isMut;
    public Decl attr;
    protected int level;
    protected IdEntry previousEntry;

    IdEntry(String id, boolean isMut, Decl attr, int level, IdEntry previousEntry) {
        this.id = id;
        this.isMut = isMut;
        this.attr = attr;
        this.level = level;
        this.previousEntry = previousEntry;
    }

}
