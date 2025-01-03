package X.TreeDrawer;

import X.Lexer.Position;
import X.Nodes.*;
import X.Nodes.Enum;
import X.Nodes.Module;

import java.awt.*;
import java.util.Arrays;

public class LayoutVisitor implements Visitor {
    private final int BORDER = 5;

    private final FontMetrics fontMetrics;

    private boolean debug;

    public LayoutVisitor(FontMetrics fontMetrics) {
        this.fontMetrics = fontMetrics;
        debug = false; // do not draw Position
    }

    void enableDebugging() {
        debug = true;
    }

    public Object visitProgram(Program ast, Object obj) {
        return layoutUnary("Program", ast.PL);
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object obj) {
        return layoutNullary("EmptyDecList");
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object obj) {
        return layoutNullary("EmptyStmtList");
    }

    public Object visitEmptyStmt(EmptyStmt ast, Object obj) {
        return layoutNullary("EmptyStmt");
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object obj) {
        return layoutNullary("EmptyParaList");
    }

    public Object visitEmptyStructList(EmptyStructList ast, Object obj) {
        return layoutNullary("EmptyStructList");
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object obj) {
        return layoutNullary("EmptyArgList");
    }


    // Declarations
    public Object visitDeclList(DeclList ast, Object obj) {
        return layoutBinary("DecList", ast.D, ast.DL);
    }

    public Object visitFunction(Function ast, Object obj) {
        return layoutQuaternary("FunDec(" + ast.TypeDef + ")", ast.T, ast.I, ast.PL, ast.S);
    }

    public Object visitGenericFunction(GenericFunction ast, Object o) {
        return layoutQuinary("GenericFunction", ast.GTL, ast.T, ast.I, ast.PL, ast.S);
    }

    public Object visitMethod(Method ast, Object obj) {
        return layoutQuinary("MethodDec(" + ast.TypeDef + ")", ast.T, ast.I, ast.PL, ast.S, ast.attachedStruct);
    }

    public Object visitGlobalVar(GlobalVar ast, Object obj) {
        return layoutTernary("G.VarDec", ast.T, ast.I, ast.E);
    }

    public Object visitLocalVar(LocalVar ast, Object obj) {
        return layoutTernary("L.VarDec", ast.T, ast.I, ast.E);
    }

    public Object visitLocalVarStmt(LocalVarStmt ast, Object obj) {
        return layoutUnary("L.VarStmt", ast.V);
    }

    public Object visitTupleDestructureAssignStmt(TupleDestructureAssignStmt ast, Object obj) {
        return layoutUnary("TupleDestructureAssignStmt", ast.TDA);
    }

    public Object visitStmtList(StmtList ast, Object obj) {
        return layoutBinary("StmtList", ast.S, ast.SL);
    }

    public Object visitIfStmt(IfStmt ast, Object obj) {
        if (ast.S2 instanceof EmptyStmt)
            return layoutBinary("IfStmt", ast.E, ast.S1);
        else
            return layoutTernary("IfStmt", ast.E, ast.S1, ast.S2);
    }

    public Object visitElseIfStmt(ElseIfStmt ast, Object obj) {
        if (ast.S2 instanceof EmptyStmt) {
            return layoutBinary("ElseIfStmt", ast.E, ast.S2);
        } else {
            return layoutTernary("ElseIFStmt", ast.E, ast.S1, ast.S2);
        }
    }

    public Object visitWhileStmt(WhileStmt ast, Object obj) {
        return layoutBinary("WhileStmt", ast.E, ast.S);
    }

    public Object visitDoWhileStmt(DoWhileStmt ast, Object obj) {
        return layoutBinary("DoWhileStmt", ast.S, ast.E);
    }

    public Object visitDecimalLiteral(DecimalLiteral ast, Object o) {
        return layoutNullary(ast.spelling);
    }

    public Object visitF32Type(F32Type ast, Object o) {
        return layoutNullary("f32");
    }

    public Object visitF64Type(F64Type ast, Object o) {
        return layoutNullary("f64");
    }

    public Object visitF32Expr(F32Expr ast, Object o) {
        return layoutUnary("F32Expr", ast.DL);
    }

    public Object visitF64Expr(F64Expr ast, Object o) {
        return layoutUnary("F64Expr", ast.DL);
    }

    public Object visitDecimalExpr(DecimalExpr ast, Object o) {
        return layoutUnary("DecimalExpr", ast.DL);
    }

    public Object visitPointerType(PointerType ast, Object o) {
        return layoutNullary(ast.t.toString() + " *");
    }

    public Object visitArrayType(ArrayType ast, Object o) {
        return layoutNullary(ast.t.toString() + "[" + ast.length +"]");
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        return layoutUnary("ArrayInitExpr", ast.AL);
    }

    public Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o) {
        return layoutBinary("ArrayIndexExpr", ast.I, ast.index);
    }

    public Object visitI8Type(I8Type ast, Object o) {
        return layoutNullary("i8");
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        return layoutNullary("'" + ast.spelling + "'");
    }

    public Object visitCharExpr(I8Expr ast, Object o) {
        if (ast.CL.isPresent()) {
            return layoutUnary("I8Expr", ast.CL.get());
        }
        return layoutUnary("I8Expr", ast.IL.get());
    }

    public Object visitCastExpr(CastExpr ast, Object o) {
        return layoutTernary("CastExpr", ast.E, ast.tFrom, ast.tTo);
    }

    public Object visitForStmt(ForStmt ast, Object obj) {
        return layoutQuaternary("ForStmt", ast.S1, ast.E2, ast.S3, ast.S);
    }

    public Object visitBreakStmt(BreakStmt ast, Object obj) {
        return layoutNullary("BrkStmt");
    }

    public Object visitContinueStmt(ContinueStmt ast, Object obj) {
        return layoutNullary("ConStmt");
    }

    public Object visitReturnStmt(ReturnStmt ast, Object obj) {
        return layoutUnary("RetStmt", ast.E);
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object obj) {
        return layoutUnary("CompStmt", ast.SL);
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object obj) {
        return layoutNullary("EmptyCompStmt");
    }

    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        return layoutNullary("EmptyExpr");
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        if (ast.isLibC) {
            return layoutBinary("@CallExpr(" + ast.TypeDef + ")", ast.I, ast.AL);
        } else {
            return layoutBinary("CallExpr(" + ast.TypeDef + ")", ast.I, ast.AL);
        }
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        String varName = "($)";
        if (ast.varName.isPresent()) {
            varName = "(" + ast.varName.get().I.spelling + ")";
        }
        if (ast.I1.isPresent() && ast.I2.isPresent()) {
            return layoutTernary("LoopStmt " + varName, ast.I1.get(), ast.I2.get(), ast.S);
        } else if (ast.I1.isPresent()){
            return layoutBinary("LoopStmt " + varName, ast.I1.get(), ast.S);
        }
        return layoutUnary("LoopStmt", ast.S);
    }


    public Object visitBinaryExpr(BinaryExpr ast, Object obj) {
        return layoutTernary("BinExp", ast.E1, ast.O, ast.E2);
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object obj) {
        return layoutBinary("UnaExp", ast.O, ast.E);
    }

    public Object visitI64Expr(I64Expr ast, Object obj) {
        return layoutUnary("I64Expr", ast.IL);
    }

    public Object visitI32Expr(I32Expr ast, Object obj) {
        return layoutUnary("I32Expr", ast.IL);
    }

    public Object visitU8Expr(U8Expr ast, Object obj) {
        return layoutUnary("U8Expr", ast.IL);
    }

    public Object visitU32Expr(U32Expr ast, Object obj) {
        return layoutUnary("U32Expr", ast.IL);
    }

    public Object visitU64Expr(U64Expr ast, Object obj) {
        return layoutUnary("U64Expr", ast.IL);
    }

    public Object visitIntExpr(IntExpr ast, Object obj) {
        return layoutUnary("IntExpr", ast.IL);
    }

    public Object visitBooleanExpr(BooleanExpr ast, Object obj) {
        return layoutUnary("BoolExp", ast.BL);
    }

    public Object visitParaList(ParaList ast, Object obj) {
        return layoutBinary("ParaLst", ast.P, ast.PL);
    }

    public Object visitParaDecl(ParaDecl ast, Object obj) {
        return layoutBinary("ParaDec", ast.T, ast.I);
    }

    // Arguments

    public Object visitArgList(Args ast, Object obj) {
        return layoutBinary("Args", ast.E, ast.EL);
    }


    public Object visitBooleanType(BooleanType ast, Object obj) {
        return layoutNullary("bool");
    }

    public Object visitI64Type(I64Type ast, Object obj) {
        return layoutNullary("i64");
    }

    public Object visitVariaticType(VariaticType ast, Object obj) {
        return layoutNullary("...");
    }

    public Object visitI32Type(I32Type ast, Object obj) {
        return layoutNullary("i32");
    }

    public Object visitU8Type(U8Type ast, Object obj) {
        return layoutNullary("u8");
    }

    public Object visitU32Type(U32Type ast, Object obj) {
        return layoutNullary("u32");
    }

    public Object visitU64Type(U64Type ast, Object obj) {
        return layoutNullary("u64");
    }

    public Object visitVoidType(VoidType ast, Object obj) {
        return layoutNullary("void");
    }

    public Object visitErrorType(ErrorType ast, Object obj) {
        return layoutNullary("err");
    }

    public Object visitIntLiteral(IntLiteral ast, Object obj) {
        return layoutNullary(ast.spelling);
    }


    public Object visitBooleanLiteral(BooleanLiteral ast, Object obj) {
        return layoutNullary(ast.spelling);
    }

    public Object visitIdent(Ident ast, Object obj) {
        if (ast.module.isPresent()) {
            return layoutNullary(ast.module.get() + "::" + ast.spelling);
        }
        return layoutNullary(ast.spelling);
    }

    public Object visitOperator(Operator ast, Object obj) {
        return layoutNullary(ast.spelling);
    }

    // Variable names

    public Object visitSimpleVar(SimpleVar ast, Object obj) {
        if (ast.isLibC) {
            return layoutUnary("@SimVar", ast.I);
        }
        return layoutUnary("SimVar", ast.I);
    }

    public Object visitVarExpr(VarExpr ast, Object obj) {
        return layoutUnary("VarExpr", ast.V);
    }

    private DrawingTree layoutCaption(String name) {
        int w = fontMetrics.stringWidth(name) + 4;
        int h = fontMetrics.getHeight() + 4;
        return new DrawingTree(name, w, h, fontMetrics);
    }

    private DrawingTree layoutNullary(String name) {
        DrawingTree dt = layoutCaption(name);
        dt.contour.upper_tail = new Polyline(0, dt.height + 2 * BORDER, null);
        dt.contour.upper_head = dt.contour.upper_tail;
        dt.contour.lower_tail = new Polyline(-dt.width - 2 * BORDER, 0, null);
        dt.contour.lower_head = new Polyline(0, dt.height + 2 * BORDER, dt.contour.lower_tail);
        return dt;
    }

    private DrawingTree layoutUnary(String name, AST child1) {
        if (debug) {
            Position pos;
            if (child1.parent != null) {
                pos = child1.parent.pos;
            } else {
                pos = new Position();
            }
            name += " " + pos.lineStart
                    + "(" + pos.charStart + ").."
                    + pos.lineFinish + "("
                    + pos.charFinish + ")";
        }
        DrawingTree dt = layoutCaption(name);
        DrawingTree d1 = (DrawingTree) child1.visit(this, null);
        dt.setChildren(new DrawingTree[]{d1});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutBinary(String name, AST child1, AST child2) {
        if (debug) {
            Position pos;
            try {
                pos = child1.parent.pos;
            } catch (Exception e) {
                pos = new Position();
            }
            name += " " + pos.lineStart
                    + "(" + pos.charStart + ").."
                    + pos.lineFinish + "("
                    + pos.charFinish + ")";
        }
        DrawingTree dt = layoutCaption(name);
        DrawingTree d1 = (DrawingTree) child1.visit(this, null);
        DrawingTree d2 = (DrawingTree) child2.visit(this, null);
        dt.setChildren(new DrawingTree[]{d1, d2});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutTernary(String name, AST child1, AST child2,
                                      AST child3) {
        if (debug) {
            Position pos = child1.parent.pos;
            name += " " + pos.lineStart
                    + "(" + pos.charStart + ").."
                    + pos.lineFinish + "("
                    + pos.charFinish + ")";
        }
        DrawingTree dt = layoutCaption(name);
        DrawingTree d1 = (DrawingTree) child1.visit(this, null);
        DrawingTree d2 = (DrawingTree) child2.visit(this, null);
        DrawingTree d3 = (DrawingTree) child3.visit(this, null);
        dt.setChildren(new DrawingTree[]{d1, d2, d3});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutQuaternary(String name, AST child1, AST child2,
                                         AST child3, AST child4) {
        if (debug) {
            Position pos = child1.parent.pos;
            name += " " + pos.lineStart
                    + "(" + pos.charStart + ").."
                    + pos.lineFinish + "("
                    + pos.charFinish + ")";
        }
        DrawingTree dt = layoutCaption(name);
        DrawingTree d1 = (DrawingTree) child1.visit(this, null);
        DrawingTree d2 = (DrawingTree) child2.visit(this, null);
        DrawingTree d3 = (DrawingTree) child3.visit(this, null);
        DrawingTree d4 = (DrawingTree) child4.visit(this, null);
        dt.setChildren(new DrawingTree[]{d1, d2, d3, d4});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutQuinary(String name, AST child1, AST child2,
                                        AST child3, AST child4, AST child5) { 
        
        if (debug) {
            Position pos;
            if (child1.parent != null) {
                pos = child1.parent.pos;
            } else {
                pos = new Position();
            }
            name += " " + pos.lineStart
                    + "(" + pos.charStart + ").."
                    + pos.lineFinish + "("
                    + pos.charFinish + ")";
        }
        DrawingTree dt = layoutCaption(name);
        DrawingTree d1 = (DrawingTree) child1.visit(this, null);
        DrawingTree d2 = (DrawingTree) child2.visit(this, null);
        DrawingTree d3 = (DrawingTree) child3.visit(this, null);
        DrawingTree d4 = (DrawingTree) child4.visit(this, null);
        DrawingTree d5 = (DrawingTree) child5.visit(this, null);
        dt.setChildren(new DrawingTree[]{d1, d2, d3, d4, d5});
        attachParent(dt, join(dt));
        return dt;
    }

    private void attachParent(DrawingTree dt, int w) {
        int y = 30;
        int x2 = (w - dt.width) / 2 - BORDER;
        int x1 = x2 + dt.width + 2 * BORDER - w;

        dt.children[0].offset.y = y + dt.height;
        dt.children[0].offset.x = x1;
        dt.contour.upper_head = new Polyline(0, dt.height,
                new Polyline(x1, y, dt.contour.upper_head));
        dt.contour.lower_head = new Polyline(0, dt.height,
                new Polyline(x2, y, dt.contour.lower_head));
    }

    private int join(DrawingTree dt) {
        int w, sum;

        dt.contour = dt.children[0].contour;
        sum = w = dt.children[0].width + 2 * BORDER;

        for (int i = 1; i < dt.children.length; i++) {
            int d = merge(dt.contour, dt.children[i].contour);
            dt.children[i].offset.x = d + w;
            dt.children[i].offset.y = 0;
            w = dt.children[i].width + 2 * BORDER;
            sum += d + w;
        }
        return sum;
    }

    private int merge(Polygon c1, Polygon c2) {
        int x, y, total, d;
        Polyline lower, upper, b;

        x = y = total = 0;
        upper = c1.lower_head;
        lower = c2.upper_head;

        while (lower != null && upper != null) {
            d = offset(x, y, lower.dx, lower.dy, upper.dx, upper.dy);
            x += d;
            total += d;

            if (y + lower.dy <= upper.dy) {
                x += lower.dx;
                y += lower.dy;
                lower = lower.link;
            } else {
                x -= upper.dx;
                y -= upper.dy;
                upper = upper.link;
            }
        }

        if (lower != null) {
            b = bridge(c1.upper_tail, 0, 0, lower, x, y);
            c1.upper_tail = (b.link != null) ? c2.upper_tail : b;
            c1.lower_tail = c2.lower_tail;
        } else {
            b = bridge(c2.lower_tail, x, y, upper, 0, 0);
            if (b.link == null) {
                c1.lower_tail = b;
            }
        }

        c1.lower_head = c2.lower_head;

        return total;
    }

    private int offset(int p1, int p2, int a1, int a2, int b1, int b2) {
        int d, s, t;

        if (b2 <= p2 || p2 + a2 <= 0) {
            return 0;
        }

        t = b2 * a1 - a2 * b1;
        if (t > 0) {
            if (p2 < 0) {
                s = p2 * a1;
                d = s / a2 - p1;
            } else if (p2 > 0) {
                s = p2 * b1;
                d = s / b2 - p1;
            } else {
                d = -p1;
            }
        } else if (b2 < p2 + a2) {
            s = (b2 - p2) * a1;
            d = b1 - (p1 + s / a2);
        } else if (b2 > p2 + a2) {
            s = (a2 + p2) * b1;
            d = s / b2 - (p1 + a1);
        } else {
            d = b1 - (p1 + a1);
        }

        return Math.max(d, 0);
    }

    private Polyline bridge(Polyline line1, int x1, int y1,
                            Polyline line2, int x2, int y2) {
        int dy, dx, s;
        Polyline r;

        dy = y2 + line2.dy - y1;
        if (line2.dy == 0) {
            dx = line2.dx;
        } else {
            s = dy * line2.dx;
            dx = s / line2.dy;
        }

        r = new Polyline(dx, dy, line2.link);
        line1.link = new Polyline(x2 + line2.dx - dx - x1, 0, r);

        return r;
    }

    public Object visitStringExpr(StringExpr ast, Object obj) {
        return layoutUnary("StrExp", ast.SL);
    }

    public Object visitEnum(Enum ast, Object o) {
        return layoutUnary("Enum" + Arrays.toString(ast.IDs), ast.I);
    }

    public Object visitMurkyType(MurkyType ast, Object o) {
        return layoutNullary("MurkyType");
    }

    public Object visitEnumType(EnumType ast, Object o) {
        return layoutUnary("EnumType", ast.E.I);
    }

    public Object visitStructType(StructType ast, Object o) {
        return layoutUnary("StructType", ast.S.I);
    }

    public Object visitEmptyStructArgs(EmptyStructArgs ast, Object o) {
        return layoutNullary("EmptyStructArgs");
    }

    public Object visitStructArgs(StructArgs ast, Object o) {
        return layoutBinary("StructArgs", ast.E, ast.SL);
    }

    public Object visitStructExpr(StructExpr ast, Object o) {
        return layoutBinary("StructExpr", ast.I, ast.SA);
    }

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        return layoutTernary("AssignmentExpr", ast.LHS, ast.O, ast.RHS);
    }

    public Object visitExprStmt(ExprStmt ast, Object o) {
        return layoutUnary("ExprStmt", ast.E);
    }

    public Object visitDerefExpr(DerefExpr ast, Object o) {
        return layoutUnary("DerefExpr", ast.E);
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {
        return layoutBinary("EnumExpr", ast.Type, ast.Entry);
    }

    public Object visitEmptyStructAccessList(EmptyStructAccessList ast, Object o) {
        return layoutNullary("EmptyStructAccessList");
    }

    public Object visitStructAccessList(StructAccessList ast, Object o) {
        return layoutBinary("StructAccessList", ast.SA, ast.SAL);
    }

    public Object visitStructAccess(StructAccess ast, Object o) {
        if (ast.arrayIndex.isPresent()) {
            return layoutQuaternary("StructAccess", ast.varName, ast.L, ast.arrayIndex.get(), ast.sourceType);
        }
        return layoutBinary("StructAccess", ast.varName, ast.L);
    }

    public Object visitDotExpr(DotExpr ast, Object o) {
        return layoutBinary("DotExpr", ast.IE, ast.E);
    }

    public Object visitMethodAccessExpr(MethodAccessExpr ast, Object o) {
        return layoutTernary("MethodAccessExpr", ast.I, ast.args, ast.next);
    }

    public Object visitMethodAccessWrapper(MethodAccessWrapper ast, Object o) {
        return layoutUnary("MethodAccessWrapper", ast.methodAccessExpr);
    }

    public Object visitUnknownType(UnknownType ast, Object o) {
        return layoutNullary("UnknownType");
    }

    public Object visitStructElem(StructElem ast, Object o) {
        if (ast.T.parent == null) {
            return layoutUnary("StructElem: " + ast.isMut, ast.I);
        }
        return layoutBinary("StructElem: " + ast.isMut, ast.T, ast.I);
    }

    public Object visitStructList(StructList ast, Object o) {
        return layoutBinary("StructList", ast.S, ast.SL);
    }

    public Object visitStruct(Struct ast, Object o) {
        return layoutBinary("Struct", ast.I, ast.SL);
    }

    public Object visitStringLiteral(StringLiteral ast, Object obj) {
        return layoutNullary("\"" + ast.spelling + "\"");
    }

    public Object visitSizeOfExpr(SizeOfExpr ast, Object o) {
        if (ast.typeV.isPresent()) {
            return layoutUnary("SizeOfExpr", ast.typeV.get());
        }
        return layoutUnary("SizeOfExpr", ast.varExpr.get());
    }

    public Object visitTypeOfExpr(TypeOfExpr ast, Object o) {
        return layoutUnary("TypeOfExpr", ast.E);
    }

    @Override
    public Object visitModule(Module ast, Object o) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitModule'");
    }

    @Override
    public Object visitImportStmt(ImportStmt ast, Object o) {
        if (ast.isSTLImport) {
            return layoutUnary("ImportStmt", ast.ident);
        }
        return layoutBinary("ImportStmt", ast.ident, ast.path);
    }

    @Override
    public Object visitUsingStmt(UsingStmt ast, Object o) {
        if (ast.isSTLImport) {
            return layoutUnary("UsingStmt", ast.ident);
        }
        return layoutBinary("UsingStmt", ast.ident, ast.path);
    }

    public Object visitNullExpr(NullExpr ast, Object o) {
        return layoutNullary("null");
    }

    public Object visitEmptyTypeList(EmptyTypeList ast, Object o) {
        return layoutNullary("EmptyTypeList");
    }

    public Object visitTypeList(TypeList ast, Object o) {
        return layoutBinary("TypeList", ast.T, ast.TL);
    }

    public Object visitTupleType(TupleType ast, Object o) {
        return layoutUnary("TupleType", ast.TL);
    }

    public Object visitTupleExpr(TupleExpr ast, Object o) {
        return layoutUnary("TupleExpr", ast.EL);
    }

    public Object visitTupleExprList(TupleExprList ast, Object o) {
        return layoutBinary("TupleExprList", ast.E, ast.EL);
    }

    public Object visitEmptyTupleExprList(EmptyTupleExprList ast, Object o) {
        return layoutNullary("EmptyTupleExprList");
    }

    public Object visitTupleAccess(TupleAccess ast, Object o) {
        return layoutBinary("TupleAccess", ast.I, ast.index);
    }

    public Object visitTupleDestructureAssign(TupleDestructureAssign ast, Object o) {
        return layoutBinary("TupleDestructureAssign", ast.idents, ast.E);
    }

    public Object visitIdentsList(IdentsList ast, Object o) {
        return layoutBinary("IdentsList", ast.I, ast.IL);
    }

    public Object visitEmptyIdentsList(EmptyIdentsList ast, Object o) {
        return layoutNullary("EmptyIdentsList");
    }

    public Object visitTrait(Trait ast, Object o) {
        return layoutBinary("Trait", ast.I, ast.TL);
    }

    public Object visitEmptyTraitList(EmptyTraitList ast, Object o) {
        return layoutNullary("EmptyTraitList");
    }

    public Object visitTraitList(TraitList ast, Object o) {
        return layoutBinary("TraitList", ast.TF, ast.L);
    } 

    public Object visitImpl(Impl ast, Object o) {
        return layoutUnary("Impl", ast.IL);
    }

    public Object visitMethodList(MethodList ast, Object o) {
        return layoutBinary("MethodList", ast.M, ast.L);
    }

    public Object visitEmptyMethodList(EmptyMethodList ast, Object o) {
        return layoutNullary("EmptyMethodList");
    }

    public Object visitExtern(Extern ast, Object o) {
        if (ast.F != null) {
            return layoutUnary("Extern", ast.F);
        } else {
            return layoutUnary("Extern", ast.G);
        }
    }

    public Object visitGenericType(GenericType ast, Object o) {
        return layoutBinary("GenericType", ast.I, ast.TL);
    }

    public Object visitGenericTypeList(GenericTypeList ast, Object o) {
        return layoutTernary("GenericTypeList", ast.I, ast.IL, ast.GTL);
    }

    public Object visitEmptyGenericTypeList(EmptyGenericTypeList ast, Object o) {
        return layoutNullary("EmptyGenericTypeList");
    }

    public Object visitImplementsList(ImplementsList ast, Object o) {
        return layoutBinary("ImplementsList", ast.I, ast.IL);
    }

    public Object visitEmptyImplementsList(EmptyImplementsList ast, Object o) {
        return layoutNullary("EmptyImplementsList");
    }

}
