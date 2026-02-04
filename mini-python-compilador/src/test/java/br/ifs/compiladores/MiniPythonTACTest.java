package br.ifs.compiladores;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class MiniPythonTACTest {

    // Helper para gerar o TAC a partir de uma String de código
    private List<TACInstruction> generateTAC(String source) {
        // 1. Léxico
        MiniPythonLexer lexer = new MiniPythonLexer(source);
        List<Token> tokens = new ArrayList<>();
        Token token = lexer.nextToken();
        while (token.type != TokenType.EOF) {
            tokens.add(token);
            token = lexer.nextToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, 0, 0));

        // 2. Sintático
        MiniPythonParser parser = new MiniPythonParser(tokens);
        List<Stmt> statements = parser.parse();

        // 3. Gerador TAC
        MiniPythonTACGenerator generator = new MiniPythonTACGenerator();
        return generator.generate(statements);
    }

    @Test
    void testArithmeticExpression() {
        // Teste de expressão matemática simples: x = 10 + 5 * 2
        String code = "x = 10 + 5 * 2\n";
        
        List<TACInstruction> tac = generateTAC(code);

        // Esperado:
        // t1 = 5 * 2
        // t2 = 10 + t1
        // x = t2
        
        assertEquals(3, tac.size(), "Deve gerar 3 instruções.");
        
        assertEquals("*", tac.get(0).op);
        assertEquals("5", tac.get(0).arg1);
        assertEquals("2", tac.get(0).arg2);
        
        assertEquals("+", tac.get(1).op);
        assertEquals("10", tac.get(1).arg1);
        
        assertEquals("x", tac.get(2).result);
    }

    @Test
    void testIfElseStructure() {
        // Teste de estrutura condicional
        String code = 
            "if x > 0:\n" +
            "    print(\"Positivo\")\n" +
            "else:\n" +
            "    print(\"Negativo\")\n";

        List<TACInstruction> tac = generateTAC(code);

        // Verifica se gerou os labels e saltos essenciais
        boolean hasIfFalse = tac.stream().anyMatch(i -> "IF_FALSE".equals(i.op));
        boolean hasGoto = tac.stream().anyMatch(i -> "GOTO".equals(i.op));
        boolean hasLabels = tac.stream().anyMatch(i -> "LABEL".equals(i.op));

        assertTrue(hasIfFalse, "Deve conter instrução IF_FALSE");
        assertTrue(hasGoto, "Deve conter instrução GOTO (para pular o else)");
        assertTrue(hasLabels, "Deve conter Labels (L1, L2...)");
    }

    @Test
    void testWhileLoop() {
        // Teste de laço de repetição
        String code = 
            "while x < 10:\n" +
            "    x = x + 1\n";

        List<TACInstruction> tac = generateTAC(code);

        // Estrutura esperada:
        // L1: (Inicio do loop)
        // ... Condição ...
        // ifFalse ... goto L2
        // ... Corpo ...
        // goto L1 (Volta pro inicio)
        // L2: (Fim)

        TACInstruction firstInstr = tac.get(0);
        assertEquals("LABEL", firstInstr.op, "Primeira instrução deve ser um Label de início");
        
        TACInstruction lastInstr = tac.get(tac.size() - 1);
        assertEquals("LABEL", lastInstr.op, "Última instrução deve ser um Label de saída");

        // Verifica se há um GOTO apontando para o Label inicial
        String startLabel = firstInstr.result;
        boolean jumpsBack = tac.stream()
                .anyMatch(i -> "GOTO".equals(i.op) && startLabel.equals(i.result));
        
        assertTrue(jumpsBack, "O loop deve ter um GOTO voltando para o início (" + startLabel + ")");
    }
}
