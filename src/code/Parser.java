package code;

import java.util.ArrayList;
import java.util.List;

import static code.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        consume(BEGIN_CODE, "Expect BEGIN_CODE at the beginning of the program.");
        //while (!isAtEnd()) {
        //    statements.add(statement());
        //}
        //if (previous().type != END_CODE) {
        //    //consume(END_CODE, "Expect END_CODE at the end of the program.");
        //    throw error(peek(), "Expect END_CODE at the end of the program.");
        //}

        while (!isAtEndCode()) {
            statements.add(declaration());
        }
        consume(END_CODE, "Expect END_CODE at the end of the program.");

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(VAR, INT, CHAR, STRING, BOOL, FLOAT)) {
                TokenType type = previous().type;
                return varDeclaration(type);
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(DISPLAY)) return printStatement();
        if (match(WHILE)) return whileStatement();

        return expressionStatement();
    }

    private Stmt if_block_statement() {
        if (match(BEGIN_IF)) return new Stmt.Block(if_block());

        return statement();
    }

    private Stmt while_block_statement() {
        if (match(BEGIN_WHILE)) return new  Stmt.Block(while_block());

        return statement();
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        if (peek().type != BEGIN_IF) {
            throw error(peek(), "Expect BEGIN_IF after if condition.");
        }
        Stmt thenBranch = if_block_statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = if_block_statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        consume(COLON, "Expect ':' after DISPLAY keyword");
        Expr value = expression();
        //consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration(TokenType type) {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
            if (initializer instanceof Expr.Literal
                    && type == TokenType.INT
                    && !(((Expr.Literal)initializer).value instanceof Integer)) {
                throw error(previous(), "Invalid assignment value. Value should be of type INT");
            } else if (initializer instanceof Expr.Literal
                    && type == TokenType.FLOAT
                    && !(((Expr.Literal)initializer).value instanceof Double)) {
                throw error(previous(), "Invalid assignment value. Value should be of type FLOAT");
            } else if (initializer instanceof Expr.Literal
                    && type == TokenType.CHAR
                    && !(((Expr.Literal)initializer).value instanceof Character)) {
                throw error(previous(), "Invalid assignment value. Value should be of type CHAR");
            } else if (initializer instanceof Expr.Literal
                    && type == TokenType.STRING
                    && !(((Expr.Literal)initializer).value instanceof String)) {
                throw error(previous(), "Invalid assignment value. Value should be of type STRING");
            } else if (initializer instanceof Expr.Literal
                    && type == TokenType.BOOL
                    && !(((Expr.Literal)initializer).value instanceof Boolean)) {
                throw error(previous(), "Invalid assignment value. Value should be of type BOOL");
            }
        }

        //consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        if (peek().type != BEGIN_WHILE) {
            throw error(peek(), "Expect BEGIN_WHILE after while condition.");
        }
        Stmt body = while_block_statement();

        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        //consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private List<Stmt> if_block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END_IF) && !isAtEnd()) {
            statements.add(declaration());
        }

        if (isAtEnd()) {
            throw error(peek(), "Expect 'END IF' before end of file.");
        }

        consume(END_IF, "Expect 'END IF' after block.");
        return statements;
    }

    private List<Stmt> while_block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END_WHILE) && !isAtEnd()) {
            statements.add(declaration());
        }

        if (isAtEnd()) {
            throw error(peek(), "Expect 'END WHILE' before end of file.");
        }

        consume(END_WHILE, "Expect 'END WHILE' after block.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(LESS_GREATER, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS, AMPERSAND)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR, PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS, PLUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, STRING_LITERAL)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        //if (match(END_CODE) && peek().type == EOF) {
        //    return new Expr.Literal(null);
        //}

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private boolean isAtEndCode() {
        return peek().type == END_CODE;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        return tokens.get((current + 1));
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Code.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            switch (peek().type) {
                case FUN:
                case INT:
                case CHAR:
                case STRING:
                case BOOL:
                case FLOAT:
                case WHILE:
                case IF:
                case DISPLAY:
                case SCAN:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}