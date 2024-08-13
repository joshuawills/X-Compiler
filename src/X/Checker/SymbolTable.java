package X.Checker;

import X.Nodes.Decl;
import X.Nodes.Function;

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

    public void print() {
        System.out.println("=====");
        IdEntry e = this.latest;
        while (e != null) {
            if (e.attr instanceof Function) {
                Function F = (Function) e.attr;
                System.out.println(e.id + ": " + F.I.spelling + F.TypeDef);
            }
            e = e.previousEntry;
        }
        System.out.println("=====");
    }

    // Find with function type def
    // Only use for FUNCTIONS!
    public Decl retrieveFunc(String id) {
        boolean searching = true;
        Decl attr = null;
        IdEntry entry = this.latest;
        while (searching) {
            if (entry == null) {
                searching = false;
            } else if (entry.id.equals(id)) {
                searching = false;
                attr = entry.attr;
            } else {
                entry = entry.previousEntry;
            }
        }
        return attr;
    }

    public Decl retrieve(String id) {
        boolean searching = true;
        Decl attr = null;
        IdEntry entry = this.latest;
        while (searching) {
            if (entry == null) {
                searching = false;
            } else if (entry.id.split("\\.")[0].equals(id)) {
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
