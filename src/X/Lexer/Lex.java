package X.Lexer;

import java.util.ArrayList;

public class Lex {

    private final MyFile file;
    private int line = 1;
    private int col = 1;
    private char currChar;
    private StringBuffer spelling;

    private static final int tabSize = 8;

    public Lex(MyFile file) {
        this.file = file;
        currChar = file.getNextChar();
    }

    public ArrayList<Token> getTokens() {
        ArrayList<Token> tokens = new ArrayList<>();
        Token currentToken;
        do {
            skip();
            spelling = new StringBuffer();
            int startingColumn = col;
            TokenType kind = nextToken();
            Position sourcePos = new Position(line, line, startingColumn, col - 1);
            currentToken = new Token(kind, spelling.toString(), sourcePos);
            tokens.add(currentToken);
        } while (currentToken.kind != TokenType.EOF);
        return tokens;
    }

    private boolean isWhiteSpace() {
        return currChar == ' ' || currChar == '\n' || currChar == '\t' ||
                currChar == '\f' || currChar == '\r';
    }

    private boolean isComment() {
        char nextChar = file.inspectChar(1);
        return currChar == '/' && (nextChar == '/' || nextChar == '*');
    }

    private void loopWhiteSpace() {
        while (isWhiteSpace()) {
            accept();
        }
    }

    private void skip() {
        loopWhiteSpace();
        if (currChar == '/' && file.inspectChar(1) == '/') {
            while (currChar != '\n') {
                if (currChar == MyFile.EOF) {
                    break;
                }
               accept();
            }
            accept();
        } else if (currChar == '/' && file.inspectChar(1) == '*') {
            while (!(currChar == '*' && file.inspectChar(1) == '/')) {
                if (currChar == MyFile.EOF) {
                    System.out.printf("ERROR: %d(%d)..%d(%d): : unterminated comment\n", line, col, line, col);
                    return;
                }
                accept();
            }
            accept();
            accept();
        }
        loopWhiteSpace();
        if (isComment()) {
            skip();
        }

    }

    private void acceptWithSpelling() {
        if (currChar == MyFile.EOF) {
            spelling.append('$');
        } else {
            spelling.append(currChar);
        }
        accept();
    }

    private void accept() {
        if (currChar == '\n') {
            line += 1;
            col = 1;
        } else if (currChar == '\t') {
            col += tabSize - (col - 1) % 8;
        } else {
            col += 1;
        }
        currChar = file.getNextChar();
    }

    private TokenType nextToken() {
        return switch (currChar) {
            case '.' -> {
                acceptWithSpelling();
                if (currChar == '.' && file.inspectChar(1) == '.') {
                    acceptWithSpelling();
                    acceptWithSpelling();
                    yield TokenType.ELLIPSIS;
                }
                yield TokenType.PERIOD;
            }
            case '@' -> {
                acceptWithSpelling();
                yield TokenType.AT_SYMBOL;
            }
            case '(' -> {
                acceptWithSpelling();
                yield TokenType.OPEN_PAREN;
            }
            case ')' -> {
                acceptWithSpelling();
                yield TokenType.CLOSE_PAREN;
            }
            case '[' -> {
                acceptWithSpelling();
                yield TokenType.LEFT_SQUARE;
            }
            case ']' -> {
                acceptWithSpelling();
                yield TokenType.RIGHT_SQUARE;
            }
            case '{' -> {
                acceptWithSpelling();
                yield TokenType.OPEN_CURLY;
            }
            case '}' -> {
                acceptWithSpelling();
                yield TokenType.CLOSE_CURLY;
            }
            case ';' -> {
                acceptWithSpelling();
                yield TokenType.SEMI;
            }
            case ',' -> {
                acceptWithSpelling();
                yield TokenType.COMMA;
            }
            case '%' -> {
                acceptWithSpelling();
                yield TokenType.MOD;
            }
            case ':' -> {
                acceptWithSpelling();
                if (currChar == ':') {
                    acceptWithSpelling();
                    yield TokenType.DOUBLE_COLON;
                } else {
                    yield TokenType.COLON;
                }
            }
            case '&' -> {
                acceptWithSpelling();
                if (currChar == '&') {
                    acceptWithSpelling();
                    yield TokenType.AND_LOGIC;
                } else {
                    yield TokenType.AMPERSAND;
                }
            }
            case '|' -> {
                acceptWithSpelling();
                if (currChar == '|') {
                    acceptWithSpelling();
                    yield TokenType.OR_LOGIC;
                } else {
                    yield TokenType.BITWISE_OR;
                }
            }
            case '!' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.NOT_EQUAL;
                } else {
                    yield TokenType.NEGATE;
                }
            }
            case '=' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.EQUAL;
                } else {
                    yield TokenType.ASSIGN;
                }
            }
            case '>' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.GREATER_EQ;
                } else {
                    yield TokenType.GREATER_THAN;
                }
            }
            case '<' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.LESS_EQ;
                } else {
                    yield TokenType.LESS_THAN;
                }
            }
            case '+' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.PLUS_EQUAL;
                } else {
                    yield TokenType.PLUS;
                }
            }
            case '-' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.DASH_EQUAL;
                } else if (currChar == '>') {
                    acceptWithSpelling();
                    yield TokenType.ARROW;
                } else {
                    yield TokenType.DASH;
                }
            }
            case '*' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.STAR_EQUAL;
                } else {
                    yield TokenType.STAR;
                }
            }
            case '/' -> {
                acceptWithSpelling();
                if (currChar == '=') {
                    acceptWithSpelling();
                    yield TokenType.F_SLASH_EQUAL;
                } else {
                    yield TokenType.F_SLASH;
                }
            }
            case '$' -> {
                acceptWithSpelling();
                yield TokenType.DOLLAR;
            }
            case '\u0000' -> TokenType.EOF;
            default -> handleOther();
        };
    }

    private TokenType handleOther() {

        if (currChar == '\'') {
            accept();
            while (currChar != '\'') {
                if (currChar == '\n' || currChar == MyFile.EOF) {
                    System.err.println("ERROR: Unterminated character");
                    return TokenType.CHAR_LIT;
                }
                if (currChar == '\\') {
                    if (!isValidEscape()) {
                        if (file.inspectChar(1) != '\n') {
                            System.err.println("ERROR: Illegal escape character");
                        }
                        acceptWithSpelling();
                    } else {
                        accept();
                        acceptEscape();
                    }
                } else {
                    acceptWithSpelling();
                }
            }
            accept();
            return TokenType.CHAR_LIT;
        }

        if (currChar == '"') {
            accept();
            while (currChar != '"') {
                if (currChar == '\n' || currChar == MyFile.EOF) {
                    System.err.println("ERROR: Unterminated string");
                    return TokenType.STRING_LIT;
                }
                if (currChar == '\\') {
                    if (!isValidEscape()) {
                        if (file.inspectChar(1) != '\n') {
                            System.err.println("ERROR: Illegal escape character");
                        }
                        acceptWithSpelling();
                    } else {
                        accept();
                        acceptEscape();
                    }
                } else {
                    acceptWithSpelling();
                }
            }
            accept();
            return TokenType.STRING_LIT;
        }

        if (isLetter()) {
            while (isLetter() || isDigit()) {
                acceptWithSpelling();
            }
            handleElseIf();
            return switch (spelling.toString()) {
                case "loop" -> TokenType.LOOP;
                case "in" -> TokenType.IN;
                case "return" -> TokenType.RETURN;
                case "fn" -> TokenType.FN;
                case "enum" -> TokenType.ENUM;
                case "struct" -> TokenType.STRUCT;
                case "let" -> TokenType.LET;
                case "i8", "i32", "i64", "f32", "f64", "bool", "void" -> TokenType.TYPE;
                case "if" -> TokenType.IF;
                case "else" -> TokenType.ELSE;
                case "else if" -> TokenType.ELIF;
                case "while" -> TokenType.WHILE;
                case "for" -> TokenType.FOR;
                case "continue" -> TokenType.CONTINUE;
                case "break" -> TokenType.BREAK;
                case "do" -> TokenType.DO;
                case "true", "false" -> TokenType.BOOL_LIT;
                case "mut" -> TokenType.MUT;
                case "size" -> TokenType.SIZE_OF;
                case "type" -> TokenType.TYPE_OF;
                case "export" -> TokenType.EXPORT;
                case "import" -> TokenType.IMPORT;
                case "as" -> TokenType.AS;
                case "null" -> TokenType.NULL;
                default -> TokenType.IDENT;
            };
        }

        if (isDigit()) {
            boolean isFloat = false;
            while (isDigit()) {
                acceptWithSpelling();
            }
            if (currChar == '.') {
                isFloat = true;
                acceptWithSpelling();
            }
            while (isDigit()) {
                acceptWithSpelling();
            }
            if (isFloat) {
                return TokenType.FLOAT_LIT;
            } else {
                return TokenType.INT_LIT;
            }
        }
        acceptWithSpelling();
        return TokenType.ERROR;
    }

    private void handleElseIf() {
        if (spelling.toString().equals("else") && nextCharElIf()) {
            acceptWithSpelling();
            acceptWithSpelling();
            acceptWithSpelling();
        }
    }

    private boolean nextCharElIf() {
        return currChar == ' ' && file.inspectChar(1) == 'i'
                && file.inspectChar(2) == 'f';
    }

    private boolean isValidEscape() {
        char nextChar = file.inspectChar(1);
        return nextChar == '\\' || nextChar == 'n' || nextChar == 'b' || nextChar == 'f' ||
                nextChar == 'r' || nextChar == 't' || nextChar == '\'' || nextChar == '"' || nextChar == '0';
    }

    private void acceptEscape() {
        switch (currChar) {
            case 'b':
                spelling.append('\b');
                break;
            case 'f':
                spelling.append('\f');
                break;
            case 'n':
                spelling.append('\n');
                break;
            case 'r':
                spelling.append('\r');
                break;
            case 't':
                spelling.append('\t');
                break;
            case '\'':
                spelling.append('\'');
                break;
            case '"':
                spelling.append('\"');
                break;
            case '\\':
                spelling.append('\\');
                break;
            case '0':
                spelling.append('\0');
                break;
        }
        accept();
    }

    private boolean isLetter() {
        return (currChar <= 'z' && currChar >= 'a') ||
                (currChar <= 'Z' && currChar >= 'A') || currChar == '_';
    }

    private boolean isDigit() {
        return currChar <= '9' && currChar >= '0';
    }

}