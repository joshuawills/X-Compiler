package X.Parser;

import X.ErrorHandler;
import X.Lexer.Position;
import X.Lexer.Token;
import X.Lexer.TokenType;
import X.Nodes.*;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Optional;

public class Parser {

    private final ArrayList<Token> tokenStream;
    private int tokenIndex = 1;
    private final ErrorHandler handler;

    private Position previousPosition;
    private Token currentToken;

    public Parser(ArrayList<Token> tokenStream, ErrorHandler handler) {
        this.tokenStream = tokenStream;
        this.handler = handler;
        previousPosition = new Position();
        currentToken = tokenStream.get(0);
    }

    void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
        Position pos = currentToken.pos;
        handler.reportError(messageTemplate, tokenQuoted, pos);
        throw (new SyntaxError());
    }

    private void accept() {
        previousPosition = currentToken.pos;
        currentToken = tokenStream.get(tokenIndex);
        tokenIndex += 1;
    }

    void start(Position pos) {
        pos.lineStart = currentToken.pos.lineStart;
        pos.charStart = currentToken.pos.charStart;
    }

    void finish(Position pos) {
        pos.lineFinish = currentToken.pos.lineStart;
        pos.charFinish = currentToken.pos.charStart;
    }

    private void match(TokenType typeExpected) throws SyntaxError {
        if (currentToken.kind == typeExpected) {
            accept();
        } else {
            syntacticError("\"%\" expected here", typeExpected.toString());
        }
    }

    private boolean tryConsume(TokenType typeExpected) throws SyntaxError {
        if (currentToken.kind == typeExpected) {
            match(typeExpected);
            return true;
        }
        return false;
    }

    public Program parseProgram() throws SyntaxError {

        Program programAST;
        Position pos = new Position();

        try {
            List dlAST = parseDeclList();
            finish(pos);
            programAST = new Program(dlAST, pos);
            if (currentToken.kind != TokenType.EOF) {
                syntacticError("\"%\" unknown type", currentToken.lexeme);
            }
            return programAST;
        } catch (SyntaxError s) {
            return null;
        }
    }

    private List parseDeclList() throws SyntaxError {

        List dlAST;
        Position pos = new Position();
        start(pos);

        if (currentToken.kind == TokenType.EOF) {
            finish(pos);
            return new EmptyDeclList(pos);
        }

        if (tryConsume(TokenType.FN)) {
            // Function
            Ident ident = parseIdent();
            List pL = parseParaList();
            match(TokenType.ARROW);
            Type tAST = parseType();
            Stmt sAST = parseCompoundStmt();
            finish(pos);
            Function function = new Function(tAST, ident, pL, sAST, pos);
            List dlAST2 = parseDeclList();
            finish(pos);
            dlAST = new DeclList(function, dlAST2, pos);
        } else {
            // Global var
            boolean isMut = tryConsume(TokenType.MUT);
            Type tAST = parseType();
            Ident iAST = parseIdent();
            Expr eAST;
            if (tryConsume(TokenType.ASSIGN)) {
                eAST = parseExpr();
            } else {
                finish(pos);
                eAST = new EmptyExpr(pos);
            }
            finish(pos);
            match(TokenType.SEMI);
            GlobalVar globalVar = new GlobalVar(tAST, iAST, eAST, pos, isMut);
            List dlAST2 = parseDeclList();
            finish(pos);
            dlAST = new DeclList(globalVar, dlAST2, pos);
        }

        return dlAST;
    }

    private Stmt parseCompoundStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.OPEN_CURLY);

        if (tryConsume(TokenType.CLOSE_CURLY)) {
            finish(pos);
            return new EmptyCompStmt(pos);
        } else {
            List slAST = parseStmtList();
            match(TokenType.CLOSE_CURLY);
            finish(pos);
            return new CompoundStmt(slAST, pos);
        }
    }

    private LocalVar parseLocalVar() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        boolean isMut = tryConsume(TokenType.MUT);
        Type tAST = parseType();
        Ident iAST = parseIdent();
        Expr eAST;
        if (tryConsume(TokenType.ASSIGN)) {
            eAST = parseExpr();
        } else {
            finish(pos);
            eAST = new EmptyExpr(pos);
        }

        finish(pos);
        match(TokenType.SEMI);
        return new LocalVar(tAST, iAST, eAST, pos, isMut);
    }

    private List parseStmtList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Stmt S = parseStmt();
        if (currentToken.kind == TokenType.CLOSE_CURLY) {
            finish(pos);
            return new StmtList(S, new EmptyStmtList(pos), pos);
        }
        finish(pos);
        return new StmtList(S, parseStmtList(), pos);

    }

    // this will NOT be called for a local-var case
    private Stmt parseStmt() throws SyntaxError {
        return switch (currentToken.kind) {
            case OPEN_CURLY -> parseCompoundStmt();
            case IF -> parseIfStmt();
            case FOR -> parseForStmt();
            case WHILE -> parseWhileStmt();
            case DO -> parseDoWhileStmt();
            case LOOP -> parseLoopStmt();
            case BREAK -> parseBreakStmt();
            case CONTINUE -> parseContinueStmt();
            case RETURN -> parseReturnStmt();
            case IDENT, DOLLAR -> parseDeclOrFuncCallStmt(); // this could also be a func call
            default -> parseLocalVarStmt();
        };
    }

    private Stmt parseDeclOrFuncCallStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Ident iAST= parseIdent();
        if (tryConsume(TokenType.OPEN_PAREN)) {
            // Func call
            List aLIST;
            if (tryConsume(TokenType.CLOSE_PAREN)) {
                finish(pos);
                aLIST = new EmptyArgList(pos);
            } else {
                aLIST = parseArgList();
                match(TokenType.CLOSE_PAREN);
            }
            finish(pos);
            if (!inFor) {
                match(TokenType.SEMI);
            }
            CallExpr E = new CallExpr(iAST, aLIST, pos);
            return new CallStmt(E, pos);
        }
        return parseDeclStmt(iAST);
    }

    private LocalVarStmt parseLocalVarStmt() throws SyntaxError {
        LocalVar vAST = parseLocalVar();
        return new LocalVarStmt(vAST, vAST.pos);
    }

    private Stmt parseDeclStmt(Ident iAST) throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr eAST = new EmptyExpr(pos);
        Operator O = null;
        boolean isMath = false;
        if (currentToken.kind == TokenType.PLUS_EQUAL || currentToken.kind == TokenType.F_SLASH_EQUAL
            || currentToken.kind == TokenType.STAR_EQUAL || currentToken.kind == TokenType.DASH_EQUAL) {
            isMath = true;
            String s = currentToken.lexeme;
            accept();
            finish(pos);
            O = new Operator(s, pos);
            eAST = parseExpr();
        } else {
            if (tryConsume(TokenType.ASSIGN)) {
                eAST = parseExpr();
            }
        }

        if (!inFor) {
            match(TokenType.SEMI);
        }
        finish(pos);
        if (isMath) {
            return new MathDeclStmt(iAST, eAST, O, pos);
        } else {
            return new DeclStmt(iAST, eAST, pos);
        }
    }

    private IfStmt parseIfStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.IF);
        Expr eAST = parseExpr();
        Stmt s1AST = parseStmt();

        finish(pos);
        Stmt s2AST = new EmptyStmt(pos);
        if (currentToken.kind == TokenType.ELIF) {
            s2AST = parseElseIfStmt();
        }

        finish(pos);
        Stmt s3AST = new EmptyStmt(pos);
        if (tryConsume(TokenType.ELSE)) {
            s3AST = parseStmt();
        }

        finish(pos);
        return new IfStmt(eAST, s1AST, s2AST, s3AST, pos);
    }

    private ElseIfStmt parseElseIfStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.ELIF);
        Expr eAST = parseExpr();
        Stmt s1AST = parseStmt();

        finish(pos);
        Stmt s2AST = new EmptyStmt(pos);
        if (currentToken.kind == TokenType.ELIF) {
            s2AST = parseElseIfStmt();
        }

        finish(pos);
        return new ElseIfStmt(eAST, s1AST, s2AST, pos);
    }

    private boolean inFor = false;


    private LoopStmt parseLoopStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.LOOP);
        Optional<Expr> I1 = Optional.empty();
        Optional<Expr> I2 = Optional.empty();
        Optional<LocalVar> V = Optional.empty();
        boolean hasIn = false;

        if (currentToken.kind == TokenType.IDENT) {
            Ident I = parseIdent();
            LocalVar LV = new LocalVar(new IntType(pos), I, new EmptyExpr(pos), pos, true);
            V = Optional.of(LV);
            hasIn = tryConsume(TokenType.IN);
        }

        if (hasIn && currentToken.kind != TokenType.OPEN_CURLY) {
            Expr L1 = parseExpr();
            finish(pos);
            I1 = Optional.of(L1);
            if (currentToken.kind != TokenType.OPEN_CURLY) {
                Expr L2 = parseExpr();
                finish(pos);
                I2 = Optional.of(L2);
            }
        }
        Stmt S = parseCompoundStmt();
        finish(pos);
        return new LoopStmt(S, I1, I2, V, pos);
    }

    private ForStmt parseForStmt() throws SyntaxError {
        inFor = true;
        Position pos = new Position();
        start(pos);
        match(TokenType.FOR);
        boolean hasParen = tryConsume(TokenType.OPEN_PAREN);
        finish(pos);
        Stmt s1AST = new EmptyStmt(pos);
        if (currentToken.kind != TokenType.SEMI) {
            s1AST = parseStmt();
        }

        finish(pos);
        Expr e2AST = new EmptyExpr(pos);
        if (currentToken.kind != TokenType.SEMI) {
            e2AST = parseExpr();
        }
        match(TokenType.SEMI);

        finish(pos);
        Stmt s3AST = new EmptyStmt(pos);
        if ((hasParen && currentToken.kind == TokenType.CLOSE_PAREN) ||
            currentToken.kind != TokenType.CLOSE_CURLY) {
            s3AST = parseStmt();
        }

        if (hasParen) {
            match(TokenType.CLOSE_PAREN);
        }

        inFor = false;
        Stmt sAST = parseStmt();
        finish(pos);
        return new ForStmt(s1AST, e2AST, s3AST, sAST, pos);
    }

    private DoWhileStmt parseDoWhileStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.DO);
        Stmt sAST = parseCompoundStmt();
        match(TokenType.WHILE);
        Expr eAST = parseExpr();
        match(TokenType.SEMI);
        finish(pos);
        return new DoWhileStmt(eAST, sAST, pos);
    }

    private WhileStmt parseWhileStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.WHILE);
        Expr eAST = parseExpr();
        Stmt sAST = parseStmt();
        finish(pos);
        return new WhileStmt(eAST, sAST, pos);
    }

    private BreakStmt parseBreakStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.BREAK);
        match(TokenType.SEMI);
        finish(pos);
        return new BreakStmt(pos);
    }

    private ContinueStmt parseContinueStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.CONTINUE);
        match(TokenType.SEMI);
        finish(pos);
        return new ContinueStmt(pos);
    }

    private ReturnStmt parseReturnStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.RETURN);
        finish(pos);
        Expr eAST = new EmptyExpr(pos);
        if (currentToken.kind != TokenType.SEMI) {
            eAST = parseExpr();
        }
        match(TokenType.SEMI);
        finish(pos);
        return new ReturnStmt(eAST, pos);
    }

    private List parseFullParaList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        ParaDecl dAST = parseParaDecl();

        if (tryConsume(TokenType.CLOSE_PAREN)) {
            finish(pos);
            return new ParaList(dAST, new EmptyParaList(pos), pos);
        }

        match(TokenType.COMMA);
        finish(pos);
        return new ParaList(dAST, parseFullParaList(), pos);
    }

    private List parseParaList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.OPEN_PAREN);
        if (tryConsume(TokenType.CLOSE_PAREN)) {
            finish(pos);
            return new EmptyParaList(pos);
        } else {
            return parseFullParaList();
        }
    }


    private ParaDecl parseParaDecl() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        boolean isMut = tryConsume(TokenType.MUT);
        Type tAST = parseType();
        Ident idAST = parseIdent();
        finish(pos);
        return new ParaDecl(tAST, idAST, pos, isMut);
    }

    private Type parseType() throws SyntaxError {

        if (currentToken.kind != TokenType.TYPE) {
            syntacticError("Expected a type, received \"%\"", currentToken.kind.toString().strip());
            return null;
        }

        Position pos = currentToken.pos;
        return switch (currentToken.lexeme) {
            case "int" -> {
                accept();
                yield new IntType(pos);
            }
            case "bool" -> {
                accept();
                yield new BooleanType(pos);
            }
            case "str" -> {
                accept();
                yield new StringType(pos);
            }
            case "void" -> {
                accept();
                yield new VoidType(pos);
            }
            default -> {
                System.out.println("Should be unreachable");
                yield null;
            }
        };
    }


    Ident parseIdent() throws SyntaxError {
        if (currentToken.kind == TokenType.IDENT || currentToken.kind == TokenType.DOLLAR) {
            previousPosition = currentToken.pos;
            String spelling = currentToken.lexeme;
            Ident I = new Ident(spelling, previousPosition);
            accept();
            return I;
        } else {
            syntacticError("identifier expected here", "");
            return null;
        }
    }

    private Expr parseExpr() throws SyntaxError {
        return parseOrExpr();
    }

    private Expr parseOrExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseAndExpr();
        while (currentToken.kind == TokenType.OR_LOGIC) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseAndExpr();
            finish(pos);
            e1AST = new BinaryExpr(e1AST, e2AST, opAST, pos);
        }
        return e1AST;
    }

    private Expr parseAndExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseEqualityExpr();
        while (currentToken.kind == TokenType.AND_LOGIC) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseEqualityExpr();
            finish(pos);
            e1AST = new BinaryExpr(e1AST, e2AST, opAST, pos);
        }
        return e1AST;
    }

    private Expr parseEqualityExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseRelationalExpr();
        while (currentToken.kind == TokenType.EQUAL || currentToken.kind == TokenType.NOT_EQUAL) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseRelationalExpr();
            finish(pos);
            e1AST = new BinaryExpr(e1AST, e2AST, opAST, pos);
        }
        return e1AST;
    }

    private Expr parseRelationalExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseAdditiveExpr();
        while (currentToken.kind == TokenType.LESS_THAN || currentToken.kind == TokenType.LESS_EQ
                || currentToken.kind == TokenType.GREATER_THAN || currentToken.kind == TokenType.GREATER_EQ) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseAdditiveExpr();
            finish(pos);
            e1AST = new BinaryExpr(e1AST, e2AST, opAST, pos);
        }
        return e1AST;
    }

    private Expr parseAdditiveExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseMultiplicativeExpr();
        while (currentToken.kind == TokenType.PLUS || currentToken.kind == TokenType.DASH) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseMultiplicativeExpr();
            finish(pos);
            e1AST = new BinaryExpr(e1AST, e2AST, opAST, pos);
        }
        return e1AST;
    }

    private Expr parseMultiplicativeExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseUnaryExpr();
        while (currentToken.kind == TokenType.MOD || currentToken.kind == TokenType.STAR || currentToken.kind == TokenType.F_SLASH) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseUnaryExpr();
            finish(pos);
            e1AST = new BinaryExpr(e1AST, e2AST, opAST, pos);
        }
        return e1AST;
    }

    private Expr parseUnaryExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        return switch (currentToken.kind) {
            case PLUS, DASH, NEGATE -> {
                Operator opAST = acceptOperator();
                Expr eAST = parseUnaryExpr();
                finish(pos);
                yield new UnaryExpr(opAST, eAST, pos);
            }
            case IDENT, DOLLAR -> {
                Ident iAST = parseIdent();
                if (tryConsume(TokenType.OPEN_PAREN)) {
                    // Function call
                    List aLIST;
                    if (tryConsume(TokenType.CLOSE_PAREN)) {
                        finish(pos);
                        aLIST = new EmptyArgList(pos);
                    } else {
                        aLIST = parseArgList();
                        match(TokenType.CLOSE_PAREN);
                    }
                    finish(pos);
                    yield new CallExpr(iAST, aLIST, pos);
                } else {
                    finish(pos);
                    Var simVAST = new SimpleVar(iAST, pos);
                    yield new VarExpr(simVAST, pos);
                }
            }
            case OPEN_PAREN -> {
                match(TokenType.OPEN_PAREN);
                Expr exprAST = parseExpr();
                match(TokenType.CLOSE_PAREN);
                yield exprAST;
            }
            case INT_LIT -> {
                IntLiteral ilAST = parseIntLiteral();
                finish(pos);
                yield new IntExpr(ilAST, pos);
            }
            case BOOL_LIT -> {
                BooleanLiteral blAST = parseBooleanLiteral();
                finish(pos);
                yield new BooleanExpr(blAST, pos);
            }
            case STRING_LIT -> {
                StringLiteral slAST = parseStringLiteral();
                finish(pos);
                yield new StringExpr(slAST, pos);
            }
            default -> {
                syntacticError("Illegal primary expression", "");
                yield null;
            }
        };
    }

    private IntLiteral parseIntLiteral() throws SyntaxError {
        IntLiteral IL = null;
        if (currentToken.kind == TokenType.INT_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            IL = new IntLiteral(spelling, previousPosition);
        } else {
            syntacticError("integer literal expected here", "");
        }
        return IL;
    }

    private BooleanLiteral parseBooleanLiteral() throws SyntaxError {
        BooleanLiteral BL = null;
        if (currentToken.kind == TokenType.BOOL_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            BL = new BooleanLiteral(spelling, previousPosition);
        } else {
            syntacticError("integer literal expected here", "");
        }
        return BL;
    }


    private StringLiteral parseStringLiteral() throws SyntaxError {
        StringLiteral SL = null;
        if (currentToken.kind == TokenType.STRING_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            SL = new StringLiteral(spelling, previousPosition);
        } else {
            syntacticError("integer literal expected here", "");
        }
        return SL;
    }

    private List parseArgList() throws SyntaxError {
        Position pos = new Position();
        start(pos);

        Expr e1AST = parseExpr();
        finish(pos);

        if (currentToken.kind == TokenType.CLOSE_PAREN) {
            finish(pos);
            return new Args(e1AST, new EmptyArgList(pos), pos);
        }

        match(TokenType.COMMA);
        List alAST = parseArgList();
        finish(pos);
        return new Args(e1AST, alAST, pos);
    }

    private Operator acceptOperator() {
        previousPosition = currentToken.pos;
        String lexeme = currentToken.lexeme;
        Operator O = new Operator(lexeme, previousPosition);
        accept();
        return O;
    }

}
