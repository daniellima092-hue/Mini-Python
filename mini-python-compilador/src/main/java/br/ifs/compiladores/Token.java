package br.ifs.compiladores;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal; // Para guardar o valor convertido (Integer, Double, String)
    public final int line;
    public final int column;

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        if (literal != null) {
            return String.format("<%s, %s, %s> @%d:%d", type, lexeme, literal, line, column);
        }
        return String.format("<%s, %s> @%d:%d", type, lexeme, line, column);
    }
}