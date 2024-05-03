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

}
