package X.Lexer;

public enum TokenType {
    EOF,
    ERROR,
    SEMI,
    INT_LIT,
    STRING_LIT,
    CHAR_LIT,
    RETURN,
    ASSIGN,
    IDENT,
    PLUS,
    DASH,
    STAR,
    F_SLASH,
    GREATER_THAN,
    GREATER_EQ,
    LESS_THAN,
    LESS_EQ,
    NOT_EQUAL,
    EQUAL,
    AND_LOGIC,
    OR_LOGIC,
    NEGATE,
    OPEN_PAREN,
    CLOSE_PAREN,
    OPEN_CURLY,
    CLOSE_CURLY,
    IF,
    ELIF,
    ELSE,
    WHILE,
    OUT,
    IN,
    PLUS_EQUAL,
    DASH_EQUAL,
    STAR_EQUAL,
    F_SLASH_EQUAL,
    MUT,
    DO,
    CONTINUE,
    BREAK,
    LOOP,
    FOR,
    FN,
    ARROW, 
    COMMA,
    PERCENT,
    BITWISE_OR,
    BITWISE_AND,
    BITWISE_XOR,
    BITWISE_LEFT_SHIFT,
    BITWISE_RIGHT_SHIFT,
    TYPE,
    ARR,
    LEFT_SQUARE,
    RIGHT_SQUARE,
    DOLLAR,
    TRUE,
    FALSE
}
