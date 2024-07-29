package X.Nodes;

public interface Visitor {

    Object visitProgram(Program ast, Object o);

    Object visitIdent(Ident ast, Object o);

    Object visitFunction(Function ast, Object o);

    Object visitGlobalVar(GlobalVar ast, Object o);

    Object visitCompoundStmt(CompoundStmt ast, Object o);

    Object visitLocalVar(LocalVar ast, Object o);

    Object visitIfStmt(IfStmt ast, Object o);

    Object visitElseIfStmt(ElseIfStmt ast, Object o);

    Object visitForStmt(ForStmt ast, Object o);

    Object visitWhileStmt(WhileStmt ast, Object o);

    Object visitBreakStmt(BreakStmt ast, Object o);

    Object visitContinueStmt(ContinueStmt ast, Object o);

    Object visitReturnStmt(ReturnStmt ast, Object o);

    Object visitDeclStmt(DeclStmt ast, Object o);

    Object visitOperator(Operator ast, Object o);

    Object visitBinaryExpr(BinaryExpr ast, Object o);

    Object visitUnaryExpr(UnaryExpr ast, Object o);

    Object visitEmptyExpr(EmptyExpr ast, Object o);

    Object visitEmptyStmt(EmptyStmt ast, Object o);

    Object visitDeclList(DeclList ast, Object o);

    Object visitStmtList(StmtList ast, Object o);

    Object visitEmptyStmtList(EmptyStmtList ast, Object o);

    Object visitBooleanExpr(BooleanExpr ast, Object o);

    Object visitBooleanLiteral(BooleanLiteral ast, Object o);

    Object visitBooleanType(BooleanType ast, Object o);

    Object visitVoidType(VoidType ast, Object o);

    Object visitErrorType(ErrorType ast, Object o);

    Object visitIntExpr(IntExpr ast, Object o);

    Object visitIntLiteral(IntLiteral ast, Object o);

    Object visitIntType(IntType ast, Object o);

    Object visitArgList(Args ast, Object o);

    Object visitParaList(ParaList ast, Object o);

    Object visitStringExpr(StringExpr ast, Object o);

    Object visitStringLiteral(StringLiteral ast, Object o);

    Object visitStringType(StringType ast, Object o);

    Object visitEmptyParaList(EmptyParaList ast, Object o);

    Object visitParaDecl(ParaDecl ast, Object o);

    Object visitEmptyCompStmt(EmptyCompStmt ast, Object o);

    Object visitEmptyDeclList(EmptyDeclList ast, Object o);

    Object visitVarExpr(VarExpr ast, Object o);

    Object visitSimpleVar(SimpleVar ast, Object o);

    Object visitCallExpr(CallExpr ast, Object o);


    Object visitEmptyArgList(EmptyArgList ast, Object o);

    Object visitLocalVarStmt(LocalVarStmt ast, Object o);

    Object visitCallStmt(CallStmt ast, Object o);

    Object visitLoopStmt(LoopStmt ast, Object o);

    Object visitMathDeclStmt(MathDeclStmt ast, Object o);

    Object visitDoWhileStmt(DoWhileStmt ast, Object o);

    Object visitFloatLiteral(FloatLiteral ast, Object o);

    Object visitFloatType(FloatType ast, Object o);

    Object visitFloatExpr(FloatExpr ast, Object o);

    Object visitPointerType(PointerType ast, Object o);

    Object visitArrayType(ArrayType ast, Object o);

    Object visitArrayInitExpr(ArrayInitExpr ast, Object o);

    Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o);

    Object visitArrDeclStmt(DeclStmt ast, Object o);
}
