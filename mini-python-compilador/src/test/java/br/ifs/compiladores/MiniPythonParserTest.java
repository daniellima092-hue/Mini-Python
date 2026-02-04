package br.ifs.compiladores;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class MiniPythonParserTest {

    // Helper para rodar o Parser
    private List<Stmt> parse(String source) {
        MiniPythonLexer lexer = new MiniPythonLexer(source);
        List<Token> tokens = new ArrayList<>();
        Token token = lexer.nextToken();
        while (token.type != TokenType.EOF) {
            tokens.add(token);
            token = lexer.nextToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, 0, 0));

        MiniPythonParser parser = new MiniPythonParser(tokens);
        return parser.parse();
    }

    // Helper para rodar o Semantic
    private void analyze(String source) {
        List<Stmt> statements = parse(source);
        MiniPythonSemantic semantic = new MiniPythonSemantic();
        semantic.analyze(statements);
    }

    @Test
    void testValidAssignmentAndPrint() {
        // Teste de Sintaxe Básica
        String code = "x = 10\nprint(x)\n";
        List<Stmt> stmts = parse(code);
        
        assertEquals(2, stmts.size());
        assertTrue(stmts.get(0) instanceof AssignStmt);
        assertTrue(stmts.get(1) instanceof PrintStmt);
    }

    @Test
    void testSyntaxErrorMissingColon() {
        // Teste de Erro Sintático (Faltou :)
        String code = "if x > 10\n    print(x)\n";
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            parse(code);
        });
        
        assertTrue(exception.getMessage().contains("Esperado ':'"));
    }

    @Test
    void testSemanticErrorUndeclaredVariable() {
        // Teste Semântico: Variável não existe
        String code = "print(z)\n"; // z nunca foi declarado
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            analyze(code);
        });
        
        assertTrue(exception.getMessage().contains("não definida"));
    }

    @Test
    void testSemanticErrorTypeMismatch() {
        // Teste Semântico: Soma de String com Int (Assumindo que sua lógica proíbe)
        // Se sua lógica permite, mude para uma operação inválida como "String - Int"
        String code = 
            "x = \"Texto\"\n" +
            "y = x - 5\n"; 
            
        Exception exception = assertThrows(RuntimeException.class, () -> {
            analyze(code);
        });
        
        assertTrue(exception.getMessage().contains("incompatível") || exception.getMessage().contains("inválida"));
    }
}
