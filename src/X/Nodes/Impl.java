package X.Nodes;

import java.util.ArrayList;
import java.util.HashMap;

import X.Lexer.Position;

public class Impl extends Decl {

    // List of methods
    public List IL;
    public Ident trait;
    public Ident struct;

    private Trait refTrait;
    public Struct refStruct;

    private HashMap<Method, Boolean> MethodToVisitedMapping = new HashMap<>();

    public Impl(List ilAST, Ident traitAST, Ident structAST, Position pos) {
        super(pos, false);
        trait = traitAST;
        struct = structAST;
        IL = ilAST;
        struct.parent = trait.parent = IL.parent = this;
    }

    public String getStructType() {
        return refStruct.I.spelling;
    }

    public void setTrait(Trait T) {
        refTrait = T;
        List TL = T.TL;
        if (!TL.isEmptyTraitList()) {
            while (true) {
                Method TF = ((TraitList) TL).TF;
                MethodToVisitedMapping.put(TF, false);
                if (((TraitList) TL).L.isEmptyTraitList()) {
                    break;
                }
                TL = ((TraitList) TL).L;
            }
        }
    }

    public boolean methodExistsOnTrait(Method M) {
        return refTrait.containsMethod(M);
    }

    // returns false if already true
    public boolean addTraitFunction(Method M) {
        Method TF = refTrait.getRelatedMethod(M);
        if (MethodToVisitedMapping.get(TF)) {
            return true;
        }
        MethodToVisitedMapping.put(TF, true);
        return false;
    }

    public ArrayList<Method> getUnimplementedMethods() {
        ArrayList<Method> unimplementedMethods = new ArrayList<>();
        for (Method TF: MethodToVisitedMapping.keySet()) {
            if (!MethodToVisitedMapping.get(TF)) {
                unimplementedMethods.add(TF);
            }
        }
        return unimplementedMethods;
    }

    public Method getRelatedMethod(String v, List PL, boolean isPointer) {
        List L = IL;
        if (!L.isEmptyMethodList()) {
            while (true) {
                Method fn = ((MethodList) L).M;
                boolean eqName = fn.I.spelling.equals(v);
                boolean eqParams = fn.PL.equals(PL);
                boolean pointerEqual = fn.attachedStruct.T.isPointer() == isPointer;
                if (eqName && eqParams && pointerEqual) {
                    return fn;
                }

                if (((MethodList) L).L.isEmptyMethodList()) {
                    break;
                }
                L = ((MethodList) L).L;
            }
        }
        return null;
    }

    public void setStruct(Struct S) {
        S.addTrait(refTrait);
        refStruct = S;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitImpl(this, o);
    }
    
}
