package X.Nodes;

import X.Lexer.Position;

public class StructExpr extends Expr {

    public Ident I;
    public List SA;

    public StructExpr(Ident iAST, List saAST, Position pos) {
        super(pos);
        I = iAST;
        SA = saAST;
        I.parent = SA.parent = this;

        if (!(SA instanceof EmptyStructArgs)) {
            StructArgs SAA = (StructArgs) SA;
            int index = 0 ;
            while (true) {
                SAA.structIndex = index;
                index += 1;
                if (SAA.SL instanceof EmptyStructArgs) {
                    break;
                }
                SAA = (StructArgs) SAA.SL;
            }
        }
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructExpr(this, o);
    }

    public int getLength() {
        if (SA instanceof EmptyStructArgs) {
            return 0;
        }
        int l = 0;
        StructArgs SLL = (StructArgs) SA;
        while (true) {
            l += 1;
            if (SLL.SL instanceof EmptyStructArgs) {
                break;
            }
            SLL = (StructArgs) SLL.SL;
        }
        return l;
    }
}
