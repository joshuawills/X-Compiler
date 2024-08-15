package X.Nodes;

import X.Lexer.Position;

import java.util.ArrayList;
import java.util.HashMap;

public class Struct extends Decl {

    public List SL;
    public Ident I;
    public boolean subTypesVisited = false;
    public int length = -1;

    public Struct(List slAST, Ident iAST, Position pos) {
        super(pos, false);
        I = iAST;
        SL = slAST;
        I.parent = SL.parent = this;
    }

    public int getLength() {
        if (length != -1) {
            return length;
        }
        if (isEmpty()) {
            return length;
        }
        int l = 0;
        StructList SLL = (StructList) SL;
        while (true) {
            l += 1;
            if (SLL.SL instanceof EmptyStructList) {
                break;
            }
            SLL = (StructList) SLL.SL;
        }
        length = l;
        return length;
    }

    public ArrayList<String> findDuplicates() {
        ArrayList<String> duplicates = new ArrayList<>();
        if (isEmpty()) {
            return duplicates;
        }

        HashMap<String, Integer> stringCountMap = new HashMap<>();
        StructList SLL = (StructList) SL;
        int l = 0;
        while (true) {
            l += 1;
            String s = SLL.S.I.spelling;
            stringCountMap.put(s, stringCountMap.getOrDefault(s, 0) + 1);
            if (SLL.SL instanceof EmptyStructList) {
                break;
            }
            SLL = (StructList) SLL.SL;
        }
        for (String s : stringCountMap.keySet()) {
            if (stringCountMap.get(s) > 1) {
                duplicates.add(s);
            }
        }
        length = l;
        return duplicates;
    }

    public boolean isEmpty() {
        length = 0;
        return SL instanceof EmptyStructList;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStruct(this, o);
    }

}
