package br.ifs.compiladores;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MiniPythonLexerTest {

    // Helper para extrair todos os tokens
    private void assertTokens(String source, TokenType... expectedTypes) {
        MiniPythonLexer lexer = new MiniPythonLexer(source);
        for (TokenType expected : expectedTypes) {
            Token token = lexer.nextToken();
            assertEquals(expected, token.type, "Token esperado: " + expected + " mas veio: " + token.type + " lexema: " + token.lexeme);
        }
        assertEquals(TokenType.EOF, lexer.nextToken().type);
    }

    @Test
    void testIndentationIfElse() {
        // Exemplo 3 [cite: 112-115]
        String code = 
            "if x > 0:\n" +
            "    print \"pos\"\n" +
            "else:\n" +
            "    print \"neg\"\n"; // Adicione \n ao final
            
        assertTokens(code,
            TokenType.IF, TokenType.IDENTIFIER, TokenType.GT, TokenType.INTEGER_LITERAL, TokenType.COLON, TokenType.NEWLINE,
            TokenType.INDENT, 
                TokenType.PRINT, TokenType.STRING_LITERAL, TokenType.NEWLINE,
            TokenType.DEDENT, 
            TokenType.ELSE, TokenType.COLON, TokenType.NEWLINE,
            TokenType.INDENT,
                TokenType.PRINT, TokenType.STRING_LITERAL, TokenType.NEWLINE, // NEWLINE do print
            TokenType.DEDENT
        );
    }

    @Test
    void testFloatAndStrings() {
        // Exemplo 1 extendido [cite: 101, 102]
        String code = "y = 5.5\nz = \"hello\"";
        assertTokens(code,
                TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.FLOAT_LITERAL, TokenType.NEWLINE,
                TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.STRING_LITERAL
        );
    }

    @Test
    void testOperatorsAndPrecedenceTokens() {
        // Exemplo 5 [cite: 124]
        String code = "a = (10 + 5) * 2";
        assertTokens(code,
            TokenType.IDENTIFIER, TokenType.ASSIGN, 
            TokenType.LPAREN, TokenType.INTEGER_LITERAL, TokenType.PLUS, TokenType.INTEGER_LITERAL, TokenType.RPAREN,
            TokenType.MUL, TokenType.INTEGER_LITERAL
        );
    }
    
    @Test
    void testBuiltIns() {
        // Exemplo 2 [cite: 107]
        String code = "x = int(input())";
        assertTokens(code,
            TokenType.IDENTIFIER, TokenType.ASSIGN, 
            TokenType.INT, TokenType.LPAREN, TokenType.INPUT, TokenType.LPAREN, TokenType.RPAREN, TokenType.RPAREN
        );
    }

    @Test
    void testWhileLoop() {
        // Exemplo 4 do PDF
        String code = 
            "x = 0\n" +
            "while x < 10:\n" +
            "    x = x + 1\n" +
            "    print x\n";
            
        assertTokens(code,
            TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.NEWLINE,
            TokenType.WHILE, TokenType.IDENTIFIER, TokenType.LT, TokenType.INTEGER_LITERAL, TokenType.COLON, TokenType.NEWLINE,
            TokenType.INDENT, 
                TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.IDENTIFIER, TokenType.PLUS, TokenType.INTEGER_LITERAL, TokenType.NEWLINE,
                TokenType.PRINT, TokenType.IDENTIFIER, TokenType.NEWLINE,
            TokenType.DEDENT
        );
    }

    @Test
    void testLexicalError() {
        String code = "x = 10 $"; // $ não existe na gramática
        MiniPythonLexer lexer = new MiniPythonLexer(code);
        lexer.nextToken(); // x
        lexer.nextToken(); // =
        lexer.nextToken(); // 10
        Token err = lexer.nextToken(); // $
        assertEquals(TokenType.ERROR, err.type);
    }
}