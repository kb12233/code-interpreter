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
    BEGIN,
    END,
    CODE,
    INT,
    CHAR,
    STRING,
    BOOL,
    FLOAT,
    AND,
    OR,
    NOT,
    IF,
    ELSE,
    WHILE,
    TRUE,
    FALSE,
    PRINT, //change to DISPLAY later

    EOF
}
