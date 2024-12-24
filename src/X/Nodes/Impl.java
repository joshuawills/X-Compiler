package X.Nodes;

import java.util.HashMap;

import X.Lexer.Position;

public class Impl extends Decl {

    // List of methods
    public List IL;
    public Ident trait;
    public Ident struct;

    private Trait refTrait;
    private Struct refStruct;

    private HashMap<TraitFunction, Boolean> MethodToVisitedMapping = new HashMap<>();

    public Impl(List ilAST, Ident traitAST, Ident structAST, Position pos) {
        super(pos, false);
        trait = traitAST;
        struct = structAST;
        IL = ilAST;
        struct.parent = trait.parent = IL.parent = this;
    }

    public void setTrait(Trait T) {
        refTrait = T;
        List TL = T.TL;
        if (!TL.isEmptyTraitList()) {
            while (true) {
                TraitFunction TF = ((TraitList) TL).TF;
                MethodToVisitedMapping.put(TF, false);
                if (((TraitList) TL).L.isEmptyTraitList()) {
                    break;
                }
                TL = ((TraitList) TL).L;
            }
        }
    }

    public void setStruct(Struct S) {
        refStruct = S;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitImpl(this, o);
    }
    
}
