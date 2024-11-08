package X.Parser;

import X.ErrorHandler;
import X.Lexer.Position;
import X.Lexer.Token;
import X.Lexer.TokenType;
import X.Nodes.*;
import X.Nodes.Enum;

import java.util.ArrayList;
import java.util.Optional;

public class Parser {

    private final ArrayList<Token> tokenStream;
    private int tokenIndex = 1;

    private final ErrorHandler handler;

    private Position previousPosition;
    private Token currentToken;
    private boolean inFor = false;


    // This is to handle the confusion about struct parsing and conditional expression
    // evaluation
    private boolean assigningType = false;

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

    private Token lookAhead() {
        return lookAhead(1);
    }

    private Token lookAhead(int i) {
        return tokenStream.get(tokenIndex + i - 1);
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
            System.out.println("Received: " + currentToken);
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

        List dlAST = parseDeclList();
        finish(pos);
        programAST = new Program(dlAST, pos);
        if (currentToken.kind != TokenType.EOF) {
            syntacticError("\"%\" unknown type", currentToken.lexeme);
        }
        return programAST;
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
        } else if (tryConsume(TokenType.ENUM)) {
            Ident ident = parseIdent();
            match(TokenType.ARROW);
            match(TokenType.OPEN_CURLY);
            ArrayList<String> args = new ArrayList<>();
            if (!tryConsume(TokenType.CLOSE_CURLY)) {
                assert(currentToken.kind == TokenType.IDENT);
                args.add(currentToken.lexeme);
                accept();
                while (!tryConsume(TokenType.CLOSE_CURLY)) {
                    match(TokenType.COMMA);
                    assert(currentToken.kind == TokenType.IDENT);
                    args.add(currentToken.lexeme);
                    accept();
                }
            }
            finish(pos);
            Enum E = new Enum(args.toArray(new String[0]), ident, pos);
            List dlAST2 = parseDeclList();
            finish(pos);
            dlAST = new DeclList(E, dlAST2, pos);
        } else if (tryConsume(TokenType.STRUCT)) {
            Ident iAST = parseIdent();
            match(TokenType.ARROW);
            List SL = parseStructList();
            finish(pos);
            Struct S = new Struct(SL, iAST, pos);
            List dlAST2 = parseDeclList();
            finish(pos);
            dlAST = new DeclList(S, dlAST2, pos);
        } else {
            match(TokenType.LET);
            boolean isMut = tryConsume(TokenType.MUT);
            Ident iAST = parseIdent();
            finish(pos);
            Type tAST = new UnknownType(pos);
            if (tryConsume(TokenType.COLON)) {
                tAST = parseType();
            }
            Expr eAST = new EmptyExpr(pos);
            if (tryConsume(TokenType.ASSIGN)) {
                assigningType = true;
                eAST = parseExpr();
                assigningType = false;
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
        match(TokenType.LET);
        boolean isMut = tryConsume(TokenType.MUT);
        Ident iAST = parseIdent();
        finish(pos);
        Type tAST = new UnknownType(pos);
        if (tryConsume(TokenType.COLON)) {
            tAST = parseType();
        }
        finish(pos);
        Expr eAST = new EmptyExpr(pos);
        if (tryConsume(TokenType.ASSIGN)) {
            assigningType = true;
            eAST = parseExpr();
            assigningType = false;
        }
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
            case LET -> parseLocalVarStmt();
            default -> {
                Position pos = new Position();
                start(pos);
                Expr E = parseExpr();
                if (!inFor) {
                    match(TokenType.SEMI);
                }
                finish(pos);
                yield new ExprStmt(E, pos);
            }
        };
    }


    private LocalVarStmt parseLocalVarStmt() throws SyntaxError {
        LocalVar vAST = parseLocalVar();
        return new LocalVarStmt(vAST, vAST.pos);
    }

    private IfStmt parseIfStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.IF);
        Expr eAST = parseExpr();
        Stmt s1AST = parseCompoundStmt();

        finish(pos);
        Stmt s2AST = new EmptyStmt(pos);
        if (currentToken.kind == TokenType.ELIF) {
            s2AST = parseElseIfStmt();
        }

        finish(pos);
        Stmt s3AST = new EmptyStmt(pos);
        if (tryConsume(TokenType.ELSE)) {
            s3AST = parseCompoundStmt();
        }

        finish(pos);
        return new IfStmt(eAST, s1AST, s2AST, s3AST, pos);
    }

    private ElseIfStmt parseElseIfStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.ELIF);
        Expr eAST = parseExpr();
        Stmt s1AST = parseCompoundStmt();

        finish(pos);
        Stmt s2AST = new EmptyStmt(pos);
        if (currentToken.kind == TokenType.ELIF) {
            s2AST = parseElseIfStmt();
        }

        finish(pos);
        return new ElseIfStmt(eAST, s1AST, s2AST, pos);
    }

    private LoopStmt parseLoopStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.LOOP);
        Optional<Expr> I1 = Optional.empty();
        Optional<Expr> I2 = Optional.empty();
        Optional<LocalVar> V = Optional.empty();
        if (currentToken.kind == TokenType.IDENT && lookAhead().kind == TokenType.IN) {
            Ident I = parseIdent();
            LocalVar LV = new LocalVar(new IntType(pos), I, new EmptyExpr(pos), pos, true);
            V = Optional.of(LV);
            match(TokenType.IN);
        }

        if (currentToken.kind != TokenType.OPEN_CURLY) {
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
        match(TokenType.SEMI);
        finish(pos);
        Expr e2AST = new EmptyExpr(pos);
        if (currentToken.kind != TokenType.SEMI) {
            e2AST = parseExpr();
        }
        match(TokenType.SEMI);
        finish(pos);
        Stmt s3AST = new EmptyStmt(pos);
        if ((hasParen && currentToken.kind == TokenType.CLOSE_PAREN) ||
            currentToken.kind != TokenType.OPEN_CURLY) {
            s3AST = parseStmt();
        }
        if (hasParen) {
            match(TokenType.CLOSE_PAREN);
        }
        inFor = false;
        Stmt sAST = parseCompoundStmt();
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
        Stmt sAST = parseCompoundStmt();
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

    private List parseFullStructList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        StructElem sAST = parseStructElem();
        if (tryConsume(TokenType.CLOSE_CURLY)) {
            finish(pos);
            return new StructList(sAST, new EmptyStructList(pos), pos);
        }
        match(TokenType.COMMA);
        finish(pos);
        return new StructList(sAST, parseFullStructList(), pos);
    }

    private List parseFullStructArgs() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr eAST = parseExpr();
        if (tryConsume(TokenType.CLOSE_CURLY)) {
           finish(pos);
           return new StructArgs(eAST, new EmptyStructArgs(pos), pos);
        }
        match(TokenType.COMMA);
        finish(pos);
        return new StructArgs(eAST, parseFullStructArgs(), pos);
    }

    private List parseStructArgs() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        if (tryConsume(TokenType.CLOSE_CURLY)) {
            finish(pos);
            return new EmptyStructArgs(pos);
        } else {
            return parseFullStructArgs();
        }
    }


    private List parseStructList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.OPEN_CURLY);
        if (tryConsume(TokenType.CLOSE_CURLY)) {
            finish(pos);
            return new EmptyStructList(pos);
        } else {
            return parseFullStructList();
        }
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
        Ident idAST = parseIdent();
        match(TokenType.COLON);
        Type tAST = parseType();
        finish(pos);
        return new ParaDecl(tAST, idAST, pos, isMut);
    }

    private StructElem parseStructElem() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        boolean isMut = tryConsume(TokenType.MUT);
        Ident idAST = parseIdent();
        match(TokenType.COLON);
        Type tAST = parseType();
        finish(pos);
        return new StructElem(tAST, idAST, pos, isMut);
    }

    private Operator parseAssignmentOperator() throws SyntaxError {
        return switch(currentToken.lexeme) {
            case "=", "+=", "-=", "*=", "/=" -> {
                Operator O = new Operator(currentToken.lexeme, currentToken.pos);
                accept();
                yield O;
            }
            default -> {
                System.out.println("unknown assignment operator: " + currentToken.lexeme);
                throw new SyntaxError();
            }
        };
    }

    private boolean isAssignmentOperator() {
        return switch(currentToken.lexeme) {
            case "=", "+=", "-=", "*=", "/=" -> true;
            default -> false;
        };
    }

    private Type parseType() throws SyntaxError {
        Position pos = currentToken.pos;
        Type t = switch (currentToken.lexeme) {
            case "int" -> {
                accept();
                yield new IntType(pos);
            }
            case "char" -> {
                accept();
                yield new CharType(pos);
            }
            case "bool" -> {
                accept();
                yield new BooleanType(pos);
            }
            case "void" -> {
                accept();
                yield new VoidType(pos);
            }
            case "float" -> {
                accept();
                yield new FloatType(pos);
            }
            // Murky type -> TBD during type evaluation
            default -> {
                if (currentToken.kind != TokenType.IDENT) {
                    syntacticError("Expected a type, received \"%\"", currentToken.kind.toString().strip());
                    yield null;
                }
                String V = currentToken.lexeme;
                accept();
                yield new MurkyType(V, pos);
            }
        };

        if (tryConsume(TokenType.LEFT_SQUARE)) {
            int length = -1;
            if (currentToken.kind == TokenType.INT_LIT) {
                length = Integer.parseInt(currentToken.lexeme);
                match(TokenType.INT_LIT);
            }
            match(TokenType.RIGHT_SQUARE);
            t = new ArrayType(pos, t, length);
        }

        // Nested pointers ?? TODO
        if (tryConsume(TokenType.STAR)) {
            finish(pos);
            t = new PointerType(pos, t);
        }
        return t;
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
        return parseAssignmentExpr();
    }

    private Expr parseAssignmentExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr E = parseOrExpr();
        if (!isAssignmentOperator()) {
            return E;
        }
        Operator O = parseAssignmentOperator();
        Expr E2 = parseAssignmentExpr();
        finish(pos);
        E.isLHSOfAssignment = true;
        return new AssignmentExpr(E, O, E2, pos);
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
            case STAR -> {
                match(TokenType.STAR);
                Expr eAST = parseUnaryExpr();
                finish(pos);
                yield new DerefExpr(eAST, pos);
            }
            case LEFT_SQUARE -> {
                match(TokenType.LEFT_SQUARE);
                List aList;
                if (tryConsume(TokenType.RIGHT_SQUARE)) {
                    finish(pos);
                    aList = new EmptyArgList(pos);
                } else {
                    aList = parseArrayInitList();
                    match(TokenType.RIGHT_SQUARE);
                }
                finish(pos);
                yield new ArrayInitExpr(aList, pos);
            }
            case PLUS, DASH, NEGATE, AMPERSAND -> {
                Operator opAST = acceptOperator();
                Expr eAST = parseUnaryExpr();
                finish(pos);
                yield new UnaryExpr(opAST, eAST, pos);
            }
            default -> parsePostFixExpression();
       };
    }

    private Expr parsePostFixExpression() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        AST E = parsePrimaryExpr();
        if (!(E instanceof Ident)) {
            return (Expr) E;
        }
        return switch(currentToken.kind) {
            case PERIOD -> {
                // Assumes E is an Ident, may come back to bite me
                finish(pos);
                yield new DotExpr((Ident) E, parsePostFixExpressionTwo(), pos);
            }
            case OPEN_PAREN -> {
                match(TokenType.OPEN_PAREN);
                List aLIST;
                if (tryConsume(TokenType.CLOSE_PAREN)) {
                    finish(pos);
                    aLIST = new EmptyArgList(pos);
                } else {
                    aLIST = parseArgList();
                    match(TokenType.CLOSE_PAREN);
                }
                finish(pos);
                yield new CallExpr((Ident) E, aLIST, pos);
            }
            case LEFT_SQUARE -> {
                match(TokenType.LEFT_SQUARE);
                Expr eAST = parseExpr();
                match(TokenType.RIGHT_SQUARE);
                finish(pos);
                yield new ArrayIndexExpr((Ident) E, eAST, pos);
            }
            default -> {
                if (assigningType && tryConsume(TokenType.OPEN_CURLY)) {
                    List saAST = parseStructArgs();
                    finish(pos);
                    yield new StructExpr((Ident) E, saAST, pos);
                }
                finish(pos);
                Var simVAST = new SimpleVar((Ident) E, pos);
                yield new VarExpr(simVAST, pos);
            }
        };
    }

    private Expr parsePostFixExpressionTwo() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        if (tryConsume(TokenType.PERIOD)) {
            Ident I = parseIdent();
            finish(pos);
            return new DotExpr(I, parsePostFixExpressionTwo(), pos);
        }
        finish(pos);
        return new EmptyExpr(pos);
    }


    private AST parsePrimaryExpr() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        return switch (currentToken.kind) {
            case INT_LIT -> {
                IntLiteral ilAST = parseIntLiteral();
                finish(pos);
                yield new IntExpr(ilAST, pos);
            }
            case FLOAT_LIT -> {
                FloatLiteral flAST = parseFloatLiteral();
                finish(pos);
                yield new FloatExpr(flAST, pos);
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
            case CHAR_LIT -> {
                CharLiteral clAST = parseCharLiteral();
                finish(pos);
                yield new CharExpr(clAST, pos);
            }
            case OPEN_PAREN -> {
                match(TokenType.OPEN_PAREN);
                Expr exprAST = parseExpr();
                match(TokenType.CLOSE_PAREN);
                yield exprAST;
            }
            case IDENT, DOLLAR -> parseIdent();
            default -> {
                System.out.println("Unrecognised primary expression: " + currentToken);
                yield null;
            }
        };
    }

    private FloatLiteral parseFloatLiteral() throws SyntaxError {
        FloatLiteral FL = null;
        if (currentToken.kind == TokenType.FLOAT_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            FL = new FloatLiteral(spelling, previousPosition);
        } else {
            syntacticError("integer literal expected here", "");
        }
        return FL;
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
            syntacticError("boolean literal expected here", "");
        }
        return BL;
    }

    private CharLiteral parseCharLiteral() throws SyntaxError {
        CharLiteral CL = null;
        if (currentToken.kind == TokenType.CHAR_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            CL = new CharLiteral(spelling, previousPosition);
        } else {
            syntacticError("char literal expected here", "");
        }
        return CL;
    }

    private StringLiteral parseStringLiteral() throws SyntaxError {
        StringLiteral SL = null;
        if (currentToken.kind == TokenType.STRING_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            SL = new StringLiteral(spelling, previousPosition);
        } else {
            syntacticError("string literal expected here", "");
        }
        return SL;
    }

    private List parseArrayInitList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr e1AST = parseExpr();
        finish(pos);
        if (currentToken.kind == TokenType.RIGHT_SQUARE) {
            return new Args(e1AST, new EmptyArgList(pos), pos);
        }
        match(TokenType.COMMA);
        List alAST = parseArrayInitList();
        finish(pos);
        return new Args(e1AST, alAST, pos);
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