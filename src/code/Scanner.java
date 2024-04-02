package code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static code.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ']': addToken(RIGHT_BRACKET); break;
            case ',': addToken(COMMA); break;
            case '\'': addToken(SINGLE_QOUTE); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ':': addToken(COLON); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH); break;
            case '%': addToken(PERCENT); break;
            case '#': addToken(SHARP); break;
            case '&': addToken(AMPERSAND); break;
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
