package X.Nodes;

import java.util.ArrayList;
import java.util.Collections;

import X.Lexer.Position;

public class GenericType extends Type {

    public Ident I;
    public List TL; // List of traits to be implemented "ImplementsList"

    public GenericType(Ident iAST, List tlAST, Position pos) {
        super(pos);
        I = iAST;
        TL = tlAST;
        TL.parent = I.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitGenericType(this, o);
    }

    public boolean assignable(Object o) {
        return o instanceof GenericType && ((GenericType) o).I.spelling.equals(I.spelling);
    }

    public boolean equals(Object o) {
        if (!(o instanceof GenericType)) {
            return false;
        }
        GenericType GT = (GenericType) o;
        return equalTraitsToImplement(TL, GT.TL);
    }

    private boolean equalTraitsToImplement(List a, List b) {
        if (a.isEmptyImplementsList() && b.isEmptyImplementsList()) {
            // Both have no traits bound
            return true;
        } else if (a.isEmptyImplementsList() || b.isEmptyImplementsList()) {
            // One has traits, the other doesn't
            return false;
        }

        ImplementsList a_one = (ImplementsList) a;
        ImplementsList b_one = (ImplementsList) b;

        // Generate lists containing all the traits they have in them
        ArrayList<String> a_list = new ArrayList<>();
        ArrayList<String> b_list = new ArrayList<>();
        while (true) {
            a_list.add(a_one.I.spelling);
            if (a_one.IL.isEmptyImplementsList()) {
                break;
            }
            a_one = (ImplementsList) a_one;
        }
        while (true) {
            b_list.add(b_one.I.spelling);
            if (b_one.IL.isEmptyImplementsList()) {
                break;
            }
            b_one = (ImplementsList) b_one;
        }

        if (a_list.size() != b_list.size()) {
            return false;
        }

        a_list.sort(String::compareTo);
        b_list.sort(String::compareTo);
        return a_list.equals(b_list);
    }

    public String getMini() {
        return "Generic" + I.spelling;
    }

    @Override
    public String toString() {
        return "Generic" + I.spelling;
    }
    
}
