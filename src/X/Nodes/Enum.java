package X.Nodes;

import X.Lexer.Position;

import java.util.ArrayList;
import java.util.HashMap;

public class Enum extends Decl {

    public String[] IDs;
    public Ident I;

    public Enum(String[] idAST, Ident iAST, Position pos) {
        super(pos, false);
        I = iAST;
        IDs = idAST;
        I.parent = this;
    }

    public boolean containsKey(String ID) {
        for (String id : IDs) {
            if (id.equals(ID)) {
                return true;
            }
        }
        return false;
    }

    public int getValue(String ID) {
        for (int i = 0; i < IDs.length; i++) {
            if (IDs[i].equals(ID)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<String> findDuplicates() {
        HashMap<String, Integer> stringCountMap = new HashMap<>();
        ArrayList<String> duplicates = new ArrayList<>();
        // Count occurrences of each string
        for (String s : IDs) {
            stringCountMap.put(s, stringCountMap.getOrDefault(s, 0) + 1);
        }
        // Find all strings that occur more than once
        for (String s : stringCountMap.keySet()) {
            if (stringCountMap.get(s) > 1) {
                duplicates.add(s);
            }
        }
        return duplicates;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEnum(this, o);
    }

}
