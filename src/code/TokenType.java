package code;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    SINGLE_QOUTE,
    COMMA,
    MINUS,
    PLUS,
    COLON,
    SLASH,
    STAR,
    PERCENT,
    SHARP,
    AMPERSAND,
    NEW_LINE,

    // One or two character tokens.
    EQUAL,
    EQUAL_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,
    LESS_GREATER,

    // Literals.
    IDENTIFIER,
    CHAR_LITERAL,
    STRING_LITERAL,
    INT_LITERAL,
    FLOAT_LITERAL,

    // Keywords.
    BEGIN_CODE,
    END_CODE,
    BEGIN_IF,
    END_IF,
    BEGIN_WHILE,
    END_WHILE,
    INT,
    CHAR,
    STRING,
    BOOL,
    FLOAT,
    NIL,
    AND,
    OR,
    NOT,
    IF,
    ELSE,
    WHILE,
    TRUE,
    FALSE,
    DISPLAY,
    SCAN,
    FUN,
    RETURN,

    EOF
}
