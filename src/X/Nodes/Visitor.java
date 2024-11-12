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

    Object visitEmptyParaList(EmptyParaList ast, Object o);

    Object visitParaDecl(ParaDecl ast, Object o);

    Object visitEmptyCompStmt(EmptyCompStmt ast, Object o);

    Object visitEmptyDeclList(EmptyDeclList ast, Object o);

    Object visitVarExpr(VarExpr ast, Object o);

    Object visitSimpleVar(SimpleVar ast, Object o);

    Object visitCallExpr(CallExpr ast, Object o);


    Object visitEmptyArgList(EmptyArgList ast, Object o);

    Object visitLocalVarStmt(LocalVarStmt ast, Object o);

    Object visitLoopStmt(LoopStmt ast, Object o);

    Object visitDoWhileStmt(DoWhileStmt ast, Object o);

    Object visitFloatLiteral(FloatLiteral ast, Object o);

    Object visitFloatType(FloatType ast, Object o);

    Object visitFloatExpr(FloatExpr ast, Object o);

    Object visitPointerType(PointerType ast, Object o);

    Object visitArrayType(ArrayType ast, Object o);

    Object visitArrayInitExpr(ArrayInitExpr ast, Object o);

    Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o);

    Object visitCharType(CharType ast, Object o);

    Object visitCharLiteral(CharLiteral ast, Object o);

    Object visitCharExpr(CharExpr ast, Object o);

    Object visitCastExpr(CastExpr ast, Object o);

    Object visitStringLiteral(StringLiteral ast, Object o);

    Object visitStringExpr(StringExpr ast, Object o);

    Object visitEnum(Enum ast, Object o);

    Object visitMurkyType(MurkyType ast, Object o);

    Object visitEnumType(EnumType ast, Object o);

    Object visitDotExpr(DotExpr ast, Object o);

    Object visitUnknownType(UnknownType ast, Object o);

    Object visitStructElem(StructElem ast, Object o);

    Object visitStructList(StructList ast, Object o);

    Object visitStruct(Struct ast, Object o);

    Object visitEmptyStructList(EmptyStructList ast, Object o);

    Object visitStructType(StructType ast, Object o);

    Object visitEmptyStructArgs(EmptyStructArgs ast, Object o);

    Object visitStructArgs(StructArgs ast, Object o);

    Object visitStructExpr(StructExpr ast, Object o);

    Object visitAssignmentExpr(AssignmentExpr ast, Object o);

    Object visitExprStmt(ExprStmt ast, Object o);

    Object visitDerefExpr(DerefExpr ast, Object o);

    Object visitEnumExpr(EnumExpr ast, Object o);

    Object visitEmptyStructAccessList(EmptyStructAccessList ast, Object o);

    Object visitStructAccessList(StructAccessList ast, Object o);

    Object visitStructAccess(StructAccess ast, Object o);

    Object visitSizeOfExpr(SizeOfExpr ast, Object o);

    Object visitModule(Module ast, Object o);
}
