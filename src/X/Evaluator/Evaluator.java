package X.Evaluator;

import X.Nodes.*;
import X.Nodes.Enum;

public class Evaluator implements Visitor {


    public static Object evalExpression(Expr ast) {
        return ast.visit(new Evaluator(), null);
    }

    public Object visitProgram(Program ast, Object o) {
        return null;
    }

    public Object visitIdent(Ident ast, Object o) {
        return null;
    }

    public Object visitFunction(Function ast, Object o) {
        return null;
    }

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        return ast.E.visit(this, o);
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        return null;
    }

    public Object visitLocalVar(LocalVar ast, Object o) {
        return null;
    }

    public Object visitIfStmt(IfStmt ast, Object o) {
        return null;
    }

    public Object visitElseIfStmt(ElseIfStmt ast, Object o) {
        return null;
    }

    public Object visitForStmt(ForStmt ast, Object o) {
        return null;
    }

    public Object visitWhileStmt(WhileStmt ast, Object o) {
        return null;
    }

    public Object visitBreakStmt(BreakStmt ast, Object o) {
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        return null;
    }

    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        return null;
    }

    public Object visitDeclStmt(DeclStmt ast, Object o) {
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        Type t = ast.E1.type;
        return switch (ast.O.spelling) {
            case "||" -> (boolean) ast.E1.visit(this, o) || (boolean) ast.E2.visit(this, o);
            case "&&" -> (boolean) ast.E1.visit(this, o) && (boolean) ast.E2.visit(this, o);
            case "==" -> {
                if (ast.E1.type.isInt() || ast.E2.type.isChar()) {
                    yield (int) ast.E1.visit(this, o) == (int) ast.E1.visit(this, o);
                } else if (ast.E1.type.isFloat()) {
                    yield (float) ast.E1.visit(this, o) == (float) ast.E1.visit(this, o);
                } else if (t.isBoolean()) {
                    yield (boolean) ast.E1.visit(this, o) == (boolean) ast.E1.visit(this, o);
                } else {
                    yield null;
                }
            }
            case "!=" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) != (int) ast.E1.visit(this, o);
                }  else if (t.isFloat()) {
                    yield (float) ast.E1.visit(this, o) != (float) ast.E1.visit(this, o);
                } else if (t.isBoolean()) {
                    yield (boolean) ast.E1.visit(this, o) != (boolean) ast.E1.visit(this, o);
                } else {
                    yield null;
                }
            }
            case "<=" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) <= (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) <= (float) ast.E2.visit(this, o);
                }
            }
            case "<" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) < (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) < (float) ast.E2.visit(this, o);
                }
            }
            case ">" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) > (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) > (float) ast.E2.visit(this, o);
                }
            }
            case ">=" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) >= (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) >= (float) ast.E2.visit(this, o);
                }
            }
            case "+" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) + (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) + (float) ast.E2.visit(this, o);
                }
            }
            case "-" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) - (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) - (float) ast.E2.visit(this, o);
                }
            }
            case "/" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) / (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) / (float) ast.E2.visit(this, o);
                }
            }
            case "*" -> {
                if (t.isInt() || t.isChar()) {
                    yield (int) ast.E1.visit(this, o) * (int) ast.E2.visit(this, o);
                } else {
                    yield (float) ast.E1.visit(this, o) * (float) ast.E2.visit(this, o);
                }
            }
            case "%" -> (int) ast.E1.visit(this, o) % (int) ast.E2.visit(this, o);
            default -> {
                System.out.println("SHOULDN'T BE REACHED IN EVALUATOR");
                yield null;
            }
        };
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        Type t = (Type) o;
        return switch (ast.O.spelling) {
            case "+" -> ast.E.visit(this, o);
            case "-" -> {
                if (t.isInt() || t.isChar()) {
                    yield -(int) ast.E.visit(this, o);
                } else if (t.isFloat()) {
                    yield -(float) ast.E.visit(this, o);
                } else {
                    System.out.println("SHOULDN'T BE REACHED IN EVALUATOR");
                    yield null;
                }
            }
            case "!" -> !(boolean) ast.E.visit(this, o);
            case "&" -> {
                System.out.println("HANDLE ADDRESS-OF EVALUATOR");
                yield null;
            }
            case "*" -> {
                System.out.println("HANDLE DEREFERENCE EVALUATOR");
                yield null;
            }
            default -> {
                System.out.println("SHOULDN'T BE REACHED IN EVALUATOR");
                yield null;
            }
        };
    }

    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        return null;
    }

    public Object visitDeclList(DeclList ast, Object o) {
        return null;
    }

    public Object visitStmtList(StmtList ast, Object o) {
        return null;
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        return ast.BL.spelling.equals("true");
    }

    public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
        return null;
    }

    public Object visitBooleanType(BooleanType ast, Object o) {
        return null;
    }

    public Object visitVoidType(VoidType ast, Object o) {
        return null;
    }

    public Object visitErrorType(ErrorType ast, Object o) {
        return null;
    }

    // integer
    public Object visitIntExpr(IntExpr ast, Object o) {
        return Integer.parseInt(ast.IL.spelling);
    }

    public Object visitIntLiteral(IntLiteral ast, Object o) {
        return null;
    }

    public Object visitIntType(IntType ast, Object o) {
        return null;
    }

    public Object visitArgList(Args ast, Object o) {
        return null;
    }

    public Object visitParaList(ParaList ast, Object o) {
        return null;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        return null;
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    public Object visitVarExpr(VarExpr ast, Object o) {
        return ast.V.visit(this, o);
    }

    public Object visitSimpleVar(SimpleVar ast, Object o) {
        return ast.I.decl.visit(this, o);
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        return null;
    }

    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        return null;
    }

    public Object visitCallStmt(CallStmt ast, Object o) {
        return null;
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        return null;
    }

    public Object visitDoWhileStmt(DoWhileStmt ast, Object o) {
        return null;
    }

    public Object visitFloatLiteral(FloatLiteral ast, Object o) {
        return null;
    }

    public Object visitFloatType(FloatType ast, Object o) {
       return null;
    }

    public Object visitFloatExpr(FloatExpr ast, Object o) {
        return Float.parseFloat(ast.FL.spelling);
    }

    public Object visitPointerType(PointerType ast, Object o) {
        // TODO
        System.out.println("TODO POINTER TYPE");
        return null;
    }

    public Object visitArrayType(ArrayType ast, Object o) {
        return null;
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        return null;
    }

    public Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o) {
        // TODO
        return null;
    }

    public Object visitArrDeclStmt(DeclStmt ast, Object o) {
        return null;
    }

    public Object visitCharType(CharType ast, Object o) {
        return null;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        return null;
    }

    public Object visitCharExpr(CharExpr ast, Object o) {
        return (int) ast.CL.spelling.charAt(0);
    }

    public Object visitCastExpr(CastExpr ast, Object o) {
        System.out.println("EVALUATOR: CastExpr");
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        return null;
    }

    public Object visitEnum(Enum ast, Object o) {
        return null;
    }

    public Object visitMurkyType(MurkyType ast, Object o) {
       return null;
    }

    public Object visitEnumType(EnumType ast, Object o) {
        return null;
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {
        System.out.println("EVALUATOR: ENUMEXPR");
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return null;
    }
}
