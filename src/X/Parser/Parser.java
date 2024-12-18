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
    private boolean acceptableModuleAccess = false;


    // This is to handle the confusion about struct parsing and conditional expression
    // evaluation
    private boolean inConditionalCheck = false;
    private boolean parsingImportStmts = true;
    private boolean parsingUsingStmts = true;

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

    private boolean inCommaSeparatedImport = false;
    private boolean inCommaSeparatedUsing = false;

    private List parseDeclList() throws SyntaxError {

        List dlAST;
        Position pos = new Position();
        start(pos);

        if (inCommaSeparatedImport) {
            Ident I = parseIdent();
            if (tryConsume(TokenType.COMMA)) {
                inCommaSeparatedImport = true;
            } else {
                inCommaSeparatedImport = false;
                match(TokenType.SEMI);
            }
            finish(pos);
            ImportStmt IS = new ImportStmt(I);
            List dlAST2 = parseDeclList();
            finish(pos);
            return new DeclList(IS, dlAST2, pos);
        } else if (inCommaSeparatedUsing) {
            Ident I = parseIdent();
            if (tryConsume(TokenType.COMMA)) {
                inCommaSeparatedUsing = true;
            } else {
                inCommaSeparatedUsing = false;
                match(TokenType.SEMI);
            }
            finish(pos);
            UsingStmt US = new UsingStmt(I);
            List dlAST2 = parseDeclList();
            finish(pos);
            return new DeclList(US, dlAST2, pos);
        }

        if (currentToken.kind != TokenType.IMPORT && currentToken.kind != TokenType.USING) {
            parsingImportStmts = false;
        }
        
        if (currentToken.kind == TokenType.EOF) {
            finish(pos);
            return new EmptyDeclList(pos);
        }

        boolean isExport = tryConsume(TokenType.EXPORT);

        if (currentToken.kind == TokenType.IMPORT && parsingImportStmts) {
            match(TokenType.IMPORT);
            if (currentToken.kind == TokenType.IDENT) {
                Ident I = parseIdent();
                finish(pos);
                if (tryConsume(TokenType.COMMA)) {
                    inCommaSeparatedImport = true;
                } else {
                    match(TokenType.SEMI);
                }
                ImportStmt IS = new ImportStmt(I);
                List dlAST2 = parseDeclList();
                finish(pos);
                dlAST = new DeclList(IS, dlAST2, pos);
            } else {
                finish(pos);
                StringExpr SE = new StringExpr(parseStringLiteral(), pos);
                match(TokenType.AS);
                Ident I = parseIdent();
                match(TokenType.SEMI);
                ImportStmt IS = new ImportStmt(SE, I);
                List dlAST2 = parseDeclList();
                finish(pos);
                dlAST = new DeclList(IS, dlAST2, pos);
            }
        }
        else if (currentToken.kind == TokenType.USING && parsingUsingStmts) {
            match(TokenType.USING);
            if (currentToken.kind == TokenType.IDENT) {
                Ident I = parseIdent();
                finish(pos);
                if (tryConsume(TokenType.COMMA)) {
                    inCommaSeparatedUsing = true;
                } else {
                    match(TokenType.SEMI);
                }
                UsingStmt US = new UsingStmt(I);
                List dlAST2 = parseDeclList();
                finish(pos);
                dlAST = new DeclList(US, dlAST2, pos);
            } else {
                finish(pos);
                StringExpr SE = new StringExpr(parseStringLiteral(), pos);
                match(TokenType.SEMI);
                UsingStmt US = new UsingStmt(SE);
                List dlAST2 = parseDeclList();
                finish(pos);
                dlAST = new DeclList(US, dlAST2, pos);
            }
        }
        else if (tryConsume(TokenType.FN)) {
            // Function
            acceptableModuleAccess = false;

            Ident I1 = null;
            Type T1 = null;
            boolean subMut = false;
            if (tryConsume(TokenType.OPEN_PAREN)) {
                subMut = tryConsume(TokenType.MUT);
                I1 = parseIdent();
                match(TokenType.COLON);
                T1 = parseType();
                match(TokenType.CLOSE_PAREN);
            }

            Ident ident = parseIdent();
            List pL = parseParaList();
            match(TokenType.ARROW);
            Type tAST = parseType();
            Stmt sAST = parseCompoundStmt();
            finish(pos);
            if (I1 != null) {
                ParaDecl PD = new ParaDecl(T1, I1, pos, subMut);
                Method method = new Method(tAST, ident, pL, sAST, PD, pos);
                if (isExport) {
                    method.setExported();
                }
                List dlAST2 = parseDeclList();
                finish(pos);
                dlAST = new DeclList(method, dlAST2, pos);
            } else {
                Function function = new Function(tAST, ident, pL, sAST, pos);
                if (isExport) {
                    function.setExported();
                }
                List dlAST2 = parseDeclList();
                finish(pos);
                dlAST = new DeclList(function, dlAST2, pos);
            }

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
            if (isExport) {
                E.setExported();
            }
            List dlAST2 = parseDeclList();
            finish(pos);
            dlAST = new DeclList(E, dlAST2, pos);
        } else if (tryConsume(TokenType.STRUCT)) {
            Ident iAST = parseIdent();
            match(TokenType.ARROW);
            List SL = parseStructList();
            finish(pos);
            Struct S = new Struct(SL, iAST, pos);
            if (isExport) {
                S.setExported();
            }
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
                eAST = parseExpr();
            }
            finish(pos);
            match(TokenType.SEMI);
            GlobalVar globalVar = new GlobalVar(tAST, iAST, eAST, pos, isMut);
            if (isExport) {
                globalVar.setExported();
            }
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

    private Decl parseLocalVar() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.LET);
        boolean isMut = tryConsume(TokenType.MUT);
        Ident iAST = parseIdent();

        List idents = null;
        if (tryConsume(TokenType.COMMA)) {
            idents = parseIdentsList();
            finish(pos);
            idents = new IdentsList(iAST, idents, pos);
        }

        finish(pos);
        Type tAST = new UnknownType(pos);
        if (tryConsume(TokenType.COLON)) {
            tAST = parseType();
        }
        finish(pos);
        Expr eAST = new EmptyExpr(pos);
        if (tryConsume(TokenType.ASSIGN)) {
            eAST = parseExpr();
        }
        match(TokenType.SEMI);
        if (idents != null) {
            return new TupleDestructureAssign(idents, eAST, tAST, pos, isMut);
        }
        return new LocalVar(tAST, iAST, eAST, pos, isMut);
    }

    private List parseIdentsList() throws SyntaxError {
        Position pos = new Position();
        start(pos);

        Ident I = parseIdent();
        SimpleVar SV = new SimpleVar(I, pos);
        if (currentToken.kind == TokenType.COLON || currentToken.kind == TokenType.ASSIGN) {
            finish(pos);
            return new IdentsList(I, new EmptyIdentsList(pos), pos);
        }
        match(TokenType.COMMA);

        return new IdentsList(I, parseIdentsList(), pos);
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
            case LET -> parseVarDeclaration();
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


    private Stmt parseVarDeclaration() throws SyntaxError {
        Decl vAST = parseLocalVar();
        switch (vAST) {
            case LocalVar l -> {
                return new LocalVarStmt(l, l.pos);
            }
            case TupleDestructureAssign t -> {
                return new TupleDestructureAssignStmt(t, t.pos);
            }
            default -> {
                System.out.println("Unknown decl type");
                throw new SyntaxError();
            }
        }
    }

    private IfStmt parseIfStmt() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.IF);
        inConditionalCheck = true;
        Expr eAST = parseExpr();
        inConditionalCheck = false;
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
        inConditionalCheck = true;
        Expr eAST = parseExpr();
        inConditionalCheck = false;
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
            LocalVar LV = new LocalVar(new I64Type(pos), I, new EmptyExpr(pos), pos, true);
            V = Optional.of(LV);
            match(TokenType.IN);
        }

        if (currentToken.kind != TokenType.OPEN_CURLY) {
            inConditionalCheck = true;
            Expr L1 = parseExpr();
            finish(pos);
            I1 = Optional.of(L1);
            if (currentToken.kind != TokenType.OPEN_CURLY) {
                Expr L2 = parseExpr();
                finish(pos);
                I2 = Optional.of(L2);
            }
            inConditionalCheck = false;
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
            inConditionalCheck = true;
            e2AST = parseExpr();
            inConditionalCheck = false;
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
        inConditionalCheck = true;
        Expr eAST = parseExpr();
        inConditionalCheck = false;
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
        Type tAST;
        if (tryConsume(TokenType.ELLIPSIS)) {
            finish(pos);
            tAST = new VariaticType(pos);
        } else {
            tAST = parseType();
        }
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
            case "i8" -> {
                accept();
                yield new I8Type(pos);
            }
            case "i32" -> {
                accept();
                yield new I32Type(pos);
            }
            case "i64" -> {
                accept();
                yield new I64Type(pos);
            }
            case "u8" -> {
                accept();
                yield new U8Type(pos);
            }
            case "u32" -> {
                accept();
                yield new U32Type(pos);
            }
            case "u64" -> {
                accept();
                yield new U64Type(pos);
            }
            case "f32" -> {
                accept();
                yield new F32Type(pos);
            }
            case "f64" -> {
                accept();
                yield new F64Type(pos);
            }
            case "bool" -> {
                accept();
                yield new BooleanType(pos);
            }
            case "void" -> {
                accept();
                yield new VoidType(pos);
            }
            // Murky type -> TBD during type evaluation
            default -> {

                // Parse a tuple type
                if (currentToken.kind == TokenType.OPEN_PAREN) {
                    match(TokenType.OPEN_PAREN);
                    List tL = parseTypeList();
                    match(TokenType.CLOSE_PAREN);
                    finish(pos);
                    yield new TupleType(tL, pos);
                }

                if (currentToken.kind != TokenType.IDENT) {
                    syntacticError("Expected a type, received \"%\"", currentToken.kind.toString().strip());
                    yield null;
                }
                acceptableModuleAccess = true;
                Ident I = parseIdent();
                acceptableModuleAccess = false;
                yield new MurkyType(I, pos);
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
            while (tryConsume(TokenType.STAR)) {
                finish(pos);
                t = new PointerType(pos, t);
            }
        }
        return t;
    }

    List parseTypeList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        if (currentToken.kind == TokenType.CLOSE_PAREN) {
            finish(pos);
            return new EmptyTypeList(pos);
        }
        
        Type T = parseType();
        if (currentToken.kind != TokenType.CLOSE_PAREN) {
            match(TokenType.COMMA);
        }
        return new TypeList(T, parseTypeList(), pos);
    }


    Ident parseIdent() throws SyntaxError {
        if (currentToken.kind == TokenType.IDENT || currentToken.kind == TokenType.DOLLAR) {
            previousPosition = currentToken.pos;
            String spelling = currentToken.lexeme;
            accept();

            String spelling2 = null;
            if (acceptableModuleAccess) {
                if (tryConsume(TokenType.DOUBLE_COLON)) {
                    assert(currentToken.kind == TokenType.IDENT);
                    spelling2 = currentToken.lexeme;
                    accept();
                }
            }

            Ident I = null;
            if (spelling2 != null) {
                I = new Ident(spelling2, spelling, previousPosition);
            } else {
                I = new Ident(spelling, previousPosition);
            }
            return I;
        } else {
            syntacticError("identifier expected here", "");
            return null;
        }
    }

    private Expr parseExpr() throws SyntaxError {

        if (currentToken.kind == TokenType.SIZE_OF) {
            return parseSizeOf();
        } else if (currentToken.kind == TokenType.TYPE_OF) {
            return parseTypeOf();
        }

        Expr E = parseAssignmentExpr();

        if (tryConsume(TokenType.AS)) {
            Position pos = E.pos;
            Type T = parseType();
            finish(pos);
            E = new CastExpr(E, new UnknownType(pos), T, pos);
            ((CastExpr) E).setManualCast();
        }

        return E;
    }

    private Expr parseTypeOf() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.TYPE_OF);
        match(TokenType.OPEN_PAREN);
        Expr E = parseExpr();
        match(TokenType.CLOSE_PAREN);
        finish(pos);
        return new TypeOfExpr(E, pos);
    }

    private Expr parseSizeOf() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        match(TokenType.SIZE_OF);
        match(TokenType.OPEN_PAREN);

        Optional<VarExpr> varExprAST = Optional.empty();
        Optional<Type> typeAST = Optional.empty();

        if(lookAhead().kind == TokenType.CLOSE_PAREN && currentToken.kind != TokenType.TYPE) {
            acceptableModuleAccess = true;
            Ident ID = parseIdent();
            acceptableModuleAccess = false;
            finish(pos);
            Var simVAST = new SimpleVar(ID, pos);
            varExprAST = Optional.of(new VarExpr(simVAST, pos));
        } else {
            // Assume type parsing
            Type T = parseType();
            finish(pos);
            typeAST = Optional.of(T);
        }

        match(TokenType.CLOSE_PAREN);
        finish(pos);
        return new SizeOfExpr(varExprAST, typeAST, pos);
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
            case NULL -> {
                match(TokenType.NULL);
                finish(pos);
                yield new NullExpr(pos);
            }
            case SIZE_OF -> {
                yield parseSizeOf();
            }
            case TYPE_OF -> {
                yield parseTypeOf();
            }
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
            case AT_SYMBOL -> {
                match(TokenType.AT_SYMBOL);
                Ident I = parseIdent();
                finish(pos);
                if (currentToken.kind != TokenType.OPEN_PAREN) {
                    SimpleVar SV = new SimpleVar(I, pos);
                    SV.isLibC = true;
                    finish(pos);
                    yield new VarExpr(SV, pos);
                }
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
                yield new CallExpr(I, aLIST, pos, true);
            }
            default -> parsePostFixExpression();
       };
    }

    private Expr parsePostFixExpression() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        AST E = parsePrimaryExpr();
        return switch(currentToken.kind) {
            case PERIOD, ARROW-> {
                boolean isPointerAccess = currentToken.kind == TokenType.ARROW;
                finish(pos);
                if (lookAhead().kind == TokenType.INT_LIT) {
                    match(TokenType.PERIOD);
                    IntLiteral IL = parseIntLiteral();
                    finish(pos);
                    yield new TupleAccess((Ident) E, new IntExpr(IL, pos), pos);
                }
                if (E.isIdent()) {
                    E = new VarExpr(new SimpleVar((Ident) E, pos), pos);
                }
                yield new DotExpr((Expr) E, parsePostFixExpressionTwo(), pos, Optional.empty(), isPointerAccess);
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
                yield new CallExpr((Ident) E, aLIST, pos, false);
            }
            case LEFT_SQUARE -> {
                match(TokenType.LEFT_SQUARE);
                Expr eAST = parseExpr();
                match(TokenType.RIGHT_SQUARE);
                if (currentToken.kind == TokenType.PERIOD || currentToken.kind == TokenType.ARROW) {
                    boolean isPointerAccess = currentToken.kind == TokenType.ARROW;
                    finish(pos);
                    if (E.isIdent()) {
                        E = new VarExpr(new SimpleVar((Ident) E, pos), pos);
                    }
                    yield new DotExpr((Expr) E, parsePostFixExpressionTwo(), pos, Optional.of(eAST), isPointerAccess);
                }
                finish(pos);
                yield new ArrayIndexExpr((Ident) E, eAST, pos);
            }
            default -> {
                if (E.isIdent()) {
                    if (!inConditionalCheck && tryConsume(TokenType.OPEN_CURLY)) {
                        List saAST = parseStructArgs();
                        finish(pos);
                        yield new StructExpr((Ident) E, saAST, pos);
                    }
                    finish(pos);
                    Var simVAST = new SimpleVar((Ident) E, pos);
                    yield new VarExpr(simVAST, pos);
                } else {
                    yield (Expr) E;
                }
            }
        };
    }

    private Expr parsePostFixExpressionTwo() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        if (currentToken.kind == TokenType.ARROW || currentToken.kind == TokenType.PERIOD) { 
            boolean isPointerAccess = tryConsume(TokenType.ARROW);
            if (!isPointerAccess) {
                match(TokenType.PERIOD);
            }
            acceptableModuleAccess = true;
            Ident I = parseIdent();
            acceptableModuleAccess = false;
            Optional<Expr> E = Optional.empty();
            if (tryConsume(TokenType.LEFT_SQUARE)) {
                E = Optional.of(parseExpr());
                match(TokenType.RIGHT_SQUARE);
            } else if (tryConsume(TokenType.OPEN_PAREN)) {
                List aList;
                if (tryConsume(TokenType.CLOSE_PAREN)) {
                    finish(pos);
                    aList = new EmptyArgList(pos);
                } else {
                    aList = parseArgList();
                    match(TokenType.CLOSE_PAREN);
                }
                finish(pos);
                return new MethodAccessExpr(I, aList, pos, parsePostFixExpressionTwo());
            }
            finish(pos);
            Expr IE = new VarExpr(new SimpleVar(I, pos), pos);
            return new DotExpr(IE, parsePostFixExpressionTwo(), pos, E, isPointerAccess);
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
                DecimalLiteral flAST = parseDecimalLiteral();
                finish(pos);
                yield new DecimalExpr(flAST, pos);
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
                yield new I8Expr(clAST, pos);
            }
            case OPEN_PAREN -> {
                match(TokenType.OPEN_PAREN);
                Expr exprAST = parseExpr();
                if (tryConsume(TokenType.COMMA)) {
                    List TEL = parseTupleExprList();
                    finish(pos);
                    yield new TupleExpr(new TupleExprList(exprAST, TEL, pos), pos);
                } else {
                    match(TokenType.CLOSE_PAREN);
                    yield exprAST;
                }
            }
            case IDENT, DOLLAR -> {
                acceptableModuleAccess = true;
                Ident I = parseIdent();
                acceptableModuleAccess = false;
                yield I;
            }
            default -> {
                System.out.println("Unrecognised primary expression: " + currentToken);
                yield null;
            }
        };
    }

    private List parseTupleExprList() throws SyntaxError {
        Position pos = new Position();
        start(pos);
        Expr E = parseExpr();
        if (tryConsume(TokenType.COMMA)) {
            finish(pos);
            return new TupleExprList(E, parseTupleExprList(), pos);
        } else {
            match(TokenType.CLOSE_PAREN);
            finish(pos);
            return new TupleExprList(E, new EmptyTupleExprList(pos), pos);
        }
    }

    private DecimalLiteral parseDecimalLiteral() throws SyntaxError {
        DecimalLiteral FL = null;
        if (currentToken.kind == TokenType.FLOAT_LIT) {
            String spelling = currentToken.lexeme;
            accept();
            FL = new DecimalLiteral(spelling, previousPosition);
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