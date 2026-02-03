package br.ifs.compiladores;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class MiniPythonLexer {
    private final String source;
    private final int length;
    private int current = 0;
    private int start = 0;
    private int line = 1;
    private int columnStart = 0; // Para rastrear colunas

    // Controle de Indentação
    private final Stack<Integer> indentStack = new Stack<>();
    private final Queue<Token> tokenBuffer = new LinkedList<>();
    private boolean atLineStart = true;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("print", TokenType.PRINT);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
        keywords.put("input", TokenType.INPUT);
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("True", TokenType.TRUE);  
        keywords.put("False", TokenType.FALSE); 
    }

    public MiniPythonLexer(String source) {
        this.source = source;
        this.length = source.length();
        this.indentStack.push(0); // Nível base de indentação
    }

    /**
     * Retorna o próximo token da fonte.
     */
    public Token nextToken() {
        // 1. Se houver tokens pendentes (ex: múltiplos DEDENTs), retorna do buffer
        if (!tokenBuffer.isEmpty()) {
            return tokenBuffer.poll();
        }

        // 2. Verifica fim do arquivo
        if (isAtEnd()) {
            // Emite DEDENTs restantes antes do EOF se necessário
            if (indentStack.peek() > 0) {
                indentStack.pop();
                return new Token(TokenType.DEDENT, "", null, line, 0);
            }
            return new Token(TokenType.EOF, "", null, line, 0);
        }

        // 3. Processamento de Indentação no início da linha
        if (atLineStart) {
            handleIndentation();
            // Se handleIndentation gerou tokens (INDENT/DEDENT), retorna o primeiro
            if (!tokenBuffer.isEmpty()) {
                return tokenBuffer.poll();
            }
        }

        start = current;
        char c = advance();

        // Ignora espaços em branco fora do início da linha
        if (c == ' ' || c == '\t' || c == '\r') {
            return nextToken();
        }

        // Tratamento de Nova Linha
        if (c == '\n') {
            line++;
            columnStart = current;
            atLineStart = true;
            // Ignora linhas em branco puras para não gerar NEWLINEs desnecessários
            // Mas em Python, NEWLINE termina statements.
            return new Token(TokenType.NEWLINE, "\n", null, line - 1, calculateCol());
        }

        atLineStart = false; // Qualquer outro caractere quebra o status de início de linha

        // Comentários(Ignorados até o fim da linha)
        if (c == '#') {
            while (peek() != '\n' && !isAtEnd()) advance();
            return nextToken(); // Retorna o próximo token real
        }

        // Operadores e Pontuação
        switch (c) {
            case '(': return makeToken(TokenType.LPAREN);
            case ')': return makeToken(TokenType.RPAREN);
            case ':': return makeToken(TokenType.COLON);
            case ',': return makeToken(TokenType.COMMA);
            case '+': return makeToken(TokenType.PLUS);
            case '-': return makeToken(TokenType.MINUS);
            case '*': return makeToken(TokenType.MUL);
            case '/': return makeToken(TokenType.DIV);
            case '=': return makeToken(match('=') ? TokenType.EQ : TokenType.ASSIGN);
            case '!':
                if (match('=')) return makeToken(TokenType.NEQ);
                else return errorToken("Esperado '=' após '!'");
            case '<': return makeToken(match('=') ? TokenType.LTE : TokenType.LT);
            case '>': return makeToken(match('=') ? TokenType.GTE : TokenType.GT);
        }

        // Literais de String
        if (c == '"' || c == '\'') {
            return string(c);
        }

        // Números
        if (isDigit(c)) {
            return number();
        }

        // Identificadores e Palavras-chave
        if (isAlpha(c)) {
            return identifier();
        }

        return errorToken("Caractere inesperado: " + c);
    }

    // --- Lógica de Indentação ---
    private void handleIndentation() {
        int spaces = 0;
        // Conta espaços/tabs a partir da posição atual (sem avançar 'current' permanentemente ainda)
        int tempCurrent = current;
        while (tempCurrent < length) {
            char c = source.charAt(tempCurrent);
            if (c == ' ') spaces++;
            else if (c == '\t') spaces += 4; // Assume tab = 4 espaços
            else break;
            tempCurrent++;
        }

        char nextChar = (tempCurrent < length) ? source.charAt(tempCurrent) : '\0';

        // Se a linha for vazia ou comentário, ignora indentação
        if (nextChar == '\n' || nextChar == '\r' || nextChar == '#') {
             // Apenas consome os espaços e retorna, mantendo atLineStart = true
             current = tempCurrent;
             return; 
        }

        // Consome os espaços
        current = tempCurrent;
        atLineStart = false; // Já processamos a indentação desta linha

        int currentIndent = spaces;
        int previousIndent = indentStack.peek();

        if (currentIndent > previousIndent) {
            indentStack.push(currentIndent);
            tokenBuffer.add(new Token(TokenType.INDENT, "", null, line, calculateCol()));
        } else if (currentIndent < previousIndent) {
            while (indentStack.peek() > currentIndent) {
                tokenBuffer.add(new Token(TokenType.DEDENT, "", null, line, calculateCol()));
                indentStack.pop();
            }
            if (indentStack.peek() != currentIndent) {
                tokenBuffer.add(errorToken("Erro de Indentação: Nível inconsistente."));
            }
        }
    }

    // --- Helpers de Leitura ---

    private Token string(char quoteType) {
        while (peek() != quoteType && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) return errorToken("String não terminada.");

        advance(); // Consome a aspa de fechamento
        String value = source.substring(start + 1, current - 1);
        return new Token(TokenType.STRING_LITERAL, source.substring(start, current), value, line, calculateCol());
    }

    private Token number() {
        while (isDigit(peek())) advance();

        // Verifica ponto flutuante
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consome o '.'
            while (isDigit(peek())) advance();
            String lexeme = source.substring(start, current);
            return new Token(TokenType.FLOAT_LITERAL, lexeme, Double.parseDouble(lexeme), line, calculateCol());
        }

        String lexeme = source.substring(start, current);
        return new Token(TokenType.INTEGER_LITERAL, lexeme, Integer.parseInt(lexeme), line, calculateCol());
    }

    private Token identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        return new Token(type, text, null, line, calculateCol());
    }

    // --- Utilitários Básicos ---
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= length) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= length;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private Token makeToken(TokenType type) {
        return new Token(type, source.substring(start, current), null, line, calculateCol());
    }

    private Token errorToken(String message) {
        return new Token(TokenType.ERROR, message, null, line, calculateCol());
    }
    
    private int calculateCol() {
        return start - columnStart + 1;
    }
}
