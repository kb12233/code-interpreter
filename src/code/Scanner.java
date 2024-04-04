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

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("INT",     INT);
        keywords.put("CHAR",    CHAR);
        keywords.put("STRING",  STRING);
        keywords.put("BOOL",    BOOL);
        keywords.put("FLOAT",   FLOAT);
        keywords.put("NIL",     NIL);
        keywords.put("AND",     AND);
        keywords.put("OR",      OR);
        keywords.put("NOT",     NOT);
        keywords.put("IF",      IF);
        keywords.put("ELSE",    ELSE);
        keywords.put("WHILE",   WHILE);
        keywords.put("TRUE",    TRUE);
        keywords.put("FALSE",   FALSE);
        keywords.put("DISPLAY", DISPLAY);
        keywords.put("SCAN",    SCAN);
        keywords.put("FUN",     FUN);
        keywords.put("RETURN",  RETURN);
    }

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
            case '\'': character(); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ':': addToken(COLON); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH); break;
            case '%': addToken(PERCENT); break;
            case '&': addToken(AMPERSAND); break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '<':
                //addToken(match('=') ? LESS_EQUAL : LESS);
                if (match('=')) {
                    addToken(LESS_EQUAL);
                } else if (match('>')) {
                    addToken(LESS_GREATER);
                } else {
                    addToken(LESS);
                }
                break;
            case '#':
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd()) advance();
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                addToken(NEW_LINE);
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Code.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);

        // Check for multi-word constructs
        if (text.equals("BEGIN") || text.equals("END")) {
            // Peek ahead to see if the next characters form one of the constructs
            int lookahead = current;
            while (lookahead < source.length() && (isAlpha(source.charAt(lookahead)) || source.charAt(lookahead) == ' ')) {
                lookahead++;
            }
            String potentialConstruct = source.substring(start, lookahead).trim();
            switch (potentialConstruct) {
                case "BEGIN CODE":
                    current = lookahead; // Update current to skip the entire construct
                    addToken(BEGIN_CODE);
                    return;
                case "END CODE":
                    current = lookahead;
                    addToken(END_CODE);
                    return;
                case "BEGIN IF":
                    current = lookahead;
                    addToken(BEGIN_IF);
                    return;
                case "END IF":
                    current = lookahead;
                    addToken(END_IF);
                    return;
                case "BEGIN WHILE":
                    current = lookahead;
                    addToken(BEGIN_WHILE);
                    return;
                case "END WHILE":
                    current = lookahead;
                    addToken(END_WHILE);
                    return;
            }
        }

        // Fallback to single-word keywords or identifiers
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        //if (type == IDENTIFIER && text.equals(text.toUpperCase())) {
        //    Code.error(line, text + " is not a recognized keyword.");
        //}
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        boolean isFloat = false;
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;

            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        if (isFloat) {
            addToken(FLOAT_LITERAL, Double.parseDouble(source.substring(start, current)));
        } else {
            addToken(INT_LITERAL, Integer.parseInt(source.substring(start, current)));
        }
    }

    private void string() {
        StringBuilder value = new StringBuilder();
        if (peek() == '#') {
            Code.error(line,
            "No escape sequence detected. To print '#', it should be surrounded with square braces (ex. [#]).");
        }
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            } else if (peek() == '[') {
                // Handle escape sequence
                advance(); // Consume '['
                switch (peek()) {
                    case ']':
                        value.append(']'); break; // Append closing square brace as it's part of the escape sequence
                    case '#':
                        value.append('#'); break;
                    case '\'':
                        value.append('\''); break;
                    case '"':
                        value.append('"'); break;
                    case '$':
                        value.append('$'); break;
                    case '&':
                        value.append('&'); break;
                    case '[':
                        value.append('['); break; // For escaping opening square brace
                    default:
                        Code.error(line, "Invalid escape sequence.");
                }
                advance(); // Move past the character after '['
                if (peek() != ']') {
                    Code.error(line, "Unterminated escape sequence.");
                }
            } else {
                value.append(peek());
            }
            advance();
        }

        if (isAtEnd()) {
            Code.error(line, "Unterminated string.");
            return;
        }

        // Skip the closing "
        advance();

        // No need to trim quotes as we built the value manually
        addToken(STRING_LITERAL, value.toString());
    }

    private void character() {
        if (isAtEnd()) {
            Code.error(line, "Unterminated character literal.");
            return;
        }

        char value;
        if (peek() == '[') { // Start of escape sequence
            advance(); // Consume '['

            switch (peek()) {
                case ']':
                    value = ']'; break; // Append closing square brace as it's part of the escape sequence
                case '#':
                    value = '#'; break;
                case '\'':
                    value = '\''; break;
                case '"':
                    value = '"'; break;
                case '$':
                    value = '$'; break;
                case '&':
                    value = '&'; break;
                case '[':
                    value = '['; break; // For escaping opening square brace
                default:
                    Code.error(line, "Invalid escape sequence in character literal.");
                    return;
            }
            advance(); // Move past the character after '['

            if (peek() != ']') {
                Code.error(line, "Unterminated escape sequence in character literal.");
                return;
            }
            advance(); // Consume closing ']'
        } else {
            value = peek(); // Regular character
            advance(); // Consume character
        }

        if (peek() != '\'') {
            Code.error(line, "Character literal too long or not properly closed.");
            return;
        }
        advance(); // Consume closing single quote

        addToken(CHAR_LITERAL, value);
    }



    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
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
