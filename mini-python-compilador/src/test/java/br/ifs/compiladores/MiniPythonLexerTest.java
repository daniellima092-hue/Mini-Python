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
        // Exemplo 3
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
void testNestedIndentation() {
    // Simula um IF aninhado com diferentes níveis de espaços
    String code = 
        "if x:\n" +           // Nível 0
        "    if y:\n" +       // Nível 4 (INDENT)
        "        print z\n" + // Nível 8 (INDENT)
        "print end\n";        // Volta para Nível 0 (DEDENT, DEDENT)

    assertTokens(code,
        TokenType.IF, TokenType.IDENTIFIER, TokenType.COLON, TokenType.NEWLINE,
        TokenType.INDENT, 
            TokenType.IF, TokenType.IDENTIFIER, TokenType.COLON, TokenType.NEWLINE,
            TokenType.INDENT, 
                TokenType.PRINT, TokenType.IDENTIFIER, TokenType.NEWLINE,
            TokenType.DEDENT, 
        TokenType.DEDENT, 
        TokenType.PRINT, TokenType.IDENTIFIER, TokenType.NEWLINE
    );  
    }

    @Test
    void testIndentationWithManySpaces() {
    // Usando 8 espaços para o primeiro nível e 16 para o segundo
    String code = 
        "while True:\n" +
        "        x = 1\n" +           // 8 espaços
        "        while x:\n" +
        "                print x\n" + // 16 espaços
        "        x = 0\n" +           // volta para 8 espaços
        "print \"fim\"\n";            // volta para 0

    assertTokens(code,
        TokenType.WHILE, TokenType.TRUE, TokenType.COLON, TokenType.NEWLINE,
        TokenType.INDENT, // Entrou nível 8
            TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.NEWLINE,
            TokenType.WHILE, TokenType.IDENTIFIER, TokenType.COLON, TokenType.NEWLINE,
            TokenType.INDENT, // Entrou nível 16
                TokenType.PRINT, TokenType.IDENTIFIER, TokenType.NEWLINE,
            TokenType.DEDENT, // Voltou para nível 8
            TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.NEWLINE,
        TokenType.DEDENT, // Voltou para nível 0
        TokenType.PRINT, TokenType.STRING_LITERAL, TokenType.NEWLINE
    );
    }

    @Test
    void testFlexibleIndentationFixed() {
    String code = 
        "if True:\n" +
        "  x = 1\n" +         // 2 espaços
        "  if x:\n" +
        "     print x\n" +    // 5 espaços
        "  print \"fim\"\n";  // O \n final ajuda a disparar o DEDENT

    assertTokens(code,
        TokenType.IF, TokenType.TRUE, TokenType.COLON, TokenType.NEWLINE,
        TokenType.INDENT, 
            TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.NEWLINE,
            TokenType.IF, TokenType.IDENTIFIER, TokenType.COLON, TokenType.NEWLINE,
            TokenType.INDENT, 
                TokenType.PRINT, TokenType.IDENTIFIER, TokenType.NEWLINE,
            TokenType.DEDENT, 
            TokenType.PRINT, TokenType.STRING_LITERAL, TokenType.NEWLINE,
        TokenType.DEDENT // Este DEDENT fecha o bloco do primeiro 'if'
    );
    }

    @Test
    void testOperatorsAndPrecedenceTokens() {
        // Exemplo 5
        String code = "a = (10 + 5) * 2";
        assertTokens(code,
            TokenType.IDENTIFIER, TokenType.ASSIGN, 
            TokenType.LPAREN, TokenType.INTEGER_LITERAL, TokenType.PLUS, TokenType.INTEGER_LITERAL, TokenType.RPAREN,
            TokenType.MUL, TokenType.INTEGER_LITERAL
        );
    }
    
    @Test
    void testBuiltIns() {
        // Exemplo 2
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
