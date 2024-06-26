package X.Checker;

import X.Nodes.Decl;

public class SymbolTable {

    private int level = 1;
    private IdEntry latest = null;

    public void openScope() {
        level += 1;
    }

    public void closeScope() {
        IdEntry entry = this.latest;
        while (entry.level == this.level) {
            entry = entry.previousEntry;
        }
        this.level -= 1;
        this.latest = entry;
    }

    public void insert(String id, boolean isMut, Decl attr) {
        this.latest = new IdEntry(id, isMut, attr, this.level, this.latest);
    }

    public Decl retrieve(String id) {
        boolean present = false, searching = true;
        Decl attr = null;
        IdEntry entry = this.latest;
        while (searching) {
            if (entry == null) {
                searching = false;
            } else if (entry.id.equals(id)) {
                present = true;
                searching = false;
                attr = entry.attr;
            } else {
                entry = entry.previousEntry;
            }
        }
        return attr;
    }

    public IdEntry retrieveOneLevel(String id) {
        IdEntry entry = this.latest;

        while (entry != null) {
            if (entry.level != this.level) {
                return null;
            }
            if (entry.id.equals(id)) {
                break;
            }
            entry = entry.previousEntry;
        }
        return entry;
    }

}
