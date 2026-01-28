package br.ifs.compiladores;

import java.util.ArrayList;
import java.util.List;

public class MiniPythonParser {
    private final List<Token> tokens;
    private int current = 0;

    public MiniPythonParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // --- API Pública ---
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            // Ignora NEWLINES extras no nível global
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            statements.add(statement());
        }
        return statements;
    }

    // --- Regras de Gramática ---

    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (check(TokenType.IDENTIFIER)) return assignment();
        
        throw error(peek(), "Esperado um comando válido.");
    }

    private Stmt ifStatement() {
        Expr condition = expression();
        consume(TokenType.COLON, "Esperado ':' após a condição.");
        consume(TokenType.NEWLINE, "Esperado nova linha antes do bloco.");
        
        Stmt thenBranch = block();
        Stmt elseBranch = null;

        // Verifica se há um 'else' alinhado
        if (match(TokenType.ELSE)) {
            consume(TokenType.COLON, "Esperado ':' após else.");
            consume(TokenType.NEWLINE, "Esperado nova linha antes do bloco else.");
            elseBranch = block();
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        Expr condition = expression();
        consume(TokenType.COLON, "Esperado ':' após a condição.");
        consume(TokenType.NEWLINE, "Esperado nova linha antes do bloco.");
        Stmt body = block();
        return new WhileStmt(condition, body);
    }

    private Stmt printStatement() {
        // print(expressao)
        consume(TokenType.LPAREN, "Esperado '(' após print.");
        Expr value = expression();
        consume(TokenType.RPAREN, "Esperado ')' após expressão.");
        consume(TokenType.NEWLINE, "Esperado nova linha após print.");
        return new PrintStmt(value);
    }

    private Stmt block() {
        consume(TokenType.INDENT, "Esperado indentação para iniciar bloco.");
        List<Stmt> statements = new ArrayList<>();
        
        while (!check(TokenType.DEDENT) && !isAtEnd()) {
            if (match(TokenType.NEWLINE)) continue; // Pula linhas vazias
            statements.add(statement());
        }
        
        consume(TokenType.DEDENT, "Esperado fim de indentação (DEDENT).");
        return new BlockStmt(statements);
    }

    private Stmt assignment() {
        Token name = advance(); // Já checamos que é IDENTIFIER
        consume(TokenType.ASSIGN, "Esperado '=' para atribuição.");
        Expr value = expression();
        consume(TokenType.NEWLINE, "Esperado nova linha após atribuição.");
        return new AssignStmt(name, value);
    }

    // --- Expressões (Precedência) ---

    private Expr expression() {
        return logicOr();
    }

    private Expr logicOr() {
        Expr expr = logicAnd();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicAnd();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.NEQ, TokenType.EQ)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GT, TokenType.GTE, TokenType.LT, TokenType.LTE)) {
            Token operator = previous();
            Expr right = term();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.DIV, TokenType.MUL)) {
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new UnaryExpr(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.INTEGER_LITERAL)) return new LiteralExpr(previous().literal);
        if (match(TokenType.FLOAT_LITERAL)) return new LiteralExpr(previous().literal);
        if (match(TokenType.STRING_LITERAL)) return new LiteralExpr(previous().literal);
        
        if (match(TokenType.IDENTIFIER)) return new VariableExpr(previous());

        if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Esperado ')' após expressão.");
            return expr;
        }

        // Chamadas de funções built-in (input, int, float)
        if (match(TokenType.INPUT, TokenType.INT, TokenType.FLOAT)) {
            Token func = previous();
            consume(TokenType.LPAREN, "Esperado '(' após " + func.lexeme);
            List<Expr> args = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                args.add(expression());
            }
            consume(TokenType.RPAREN, "Esperado ')' após argumentos.");
            return new CallExpr(func, args);
        }

        throw error(peek(), "Expressão esperada.");
    }

    // --- Helpers ---

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException("Erro Sintático na linha " + token.line + ": " + message);
    }
}