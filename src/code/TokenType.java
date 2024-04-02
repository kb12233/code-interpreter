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
    BANG,
    BANG_EQUAL,
    EQUAL,
    EQUAL_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER_LESS,

    // Literals.
    IDENTIFIER,
    CHAR_LITERAL,
    STRING_LITERAL,
    INT_LITERAL,
    BOOL_LITERAL,
    FLOAT_LITERAL,

    // Keywords.
    BEGIN,
    END,
    CODE,
    INT,
    CHAR,
    BOOL,
    FLOAT,
    AND,
    OR,
    NOT,
    IF,
    ELSE,
    WHILE,
    //TRUE,
    //FALSE,
    PRINT, //change to DISPLAY later

    EOF
}
