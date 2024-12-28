package X.Nodes;

import java.util.ArrayList;
import java.util.HashMap;

import X.Lexer.Position;

public class Trait extends Decl {

    public List TL; // Traits list

    public Trait(List tlAST, Ident nameAST, Position pos) {
        super(pos, false);
        TL = tlAST;
        I = nameAST;
        TL.parent = I.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTrait(this, o);
    }

    public ArrayList<String> findDuplicates() {
        ArrayList<String> fns = new ArrayList<>();
        HashMap<String, Integer> nameToCountMapping = new HashMap<>();

        List L = TL;
        if (!L.isEmptyTraitList()) {
            while (true) {
                TraitFunction fn = ((TraitList) L).TF;
                if (!nameToCountMapping.containsKey(fn.I.spelling)) {
                    nameToCountMapping.put(fn.I.spelling, 1);
                } else {
                    int v = nameToCountMapping.get(fn.I.spelling);
                    nameToCountMapping.put(fn.I.spelling, v + 1);
                }

                if (((TraitList) L).L.isEmptyTraitList()) {
                    break;
                }
                L = ((TraitList) L).L;
            }
        }

        for (String v: nameToCountMapping.keySet()) {
            if (nameToCountMapping.get(v) > 1) {
                fns.add(v);
            }
        }

        return fns;
    }

    public TraitFunction getRelatedTF(Method M) {
        List L = TL;
        if (!L.isEmptyTraitList()) {
            while (true) {
                TraitFunction fn = ((TraitList) L).TF;
                boolean eqName = fn.I.spelling.equals(M.I.spelling);
                boolean eqParams = fn.PL.equals(M.PL);
                boolean eqType = fn.T.equals(M.T);
                boolean pointerEqual = fn.isPointer == M.attachedStruct.T.isPointer();
                boolean eqMut = fn.isMut == M.isMut;
                if (eqName && eqParams && eqType && pointerEqual && eqMut) {
                    return fn;
                }

                if (((TraitList) L).L.isEmptyTraitList()) {
                    break;
                }
                L = ((TraitList) L).L;
            }
        }

        return null;
    }

    public boolean containsMethod(Method M) {
        List L = TL;
        if (!L.isEmptyTraitList()) {
            while (true) {
                TraitFunction fn = ((TraitList) L).TF;
                boolean eqName = fn.I.spelling.equals(M.I.spelling);
                boolean eqParams = fn.PL.equals(M.PL);
                boolean eqType = fn.T.equals(M.T);
                boolean pointerEqual = fn.isPointer == M.attachedStruct.T.isPointer();
                boolean eqMut = fn.isMut == M.isMut;
                if (eqName && eqParams && eqType && pointerEqual && eqMut) {
                    return true;
                }

                if (((TraitList) L).L.isEmptyTraitList()) {
                    break;
                }
                L = ((TraitList) L).L;
            }
        }

        return false;
    }
    
}
