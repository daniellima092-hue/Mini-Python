package br.ifs.compiladores;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniPythonSemantic {
    
    // Tabela de Símbolos Simples: Nome da Variável -> Tipo ("INT", "FLOAT", "STRING")
    private Map<String, String> symbolTable = new HashMap<>();

    public void analyze(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            analyzeStmt(stmt);
        }
    }

    private void analyzeStmt(Stmt stmt) {
        if (stmt instanceof BlockStmt) {
            for (Stmt s : ((BlockStmt) stmt).statements) analyzeStmt(s);
        } 
        else if (stmt instanceof PrintStmt) {
            getExprType(((PrintStmt) stmt).expression);
        } 
        else if (stmt instanceof AssignStmt) {
            AssignStmt s = (AssignStmt) stmt;
            String type = getExprType(s.value);
            // Salva ou atualiza o tipo da variável na tabela
            symbolTable.put(s.name.lexeme, type);
        } 
        else if (stmt instanceof IfStmt) {
            IfStmt s = (IfStmt) stmt;
            getExprType(s.condition); // Verifica validade da expressão
            analyzeStmt(s.thenBranch);
            if (s.elseBranch != null) analyzeStmt(s.elseBranch);
        }
        else if (stmt instanceof WhileStmt) {
            WhileStmt s = (WhileStmt) stmt;
            getExprType(s.condition);
            analyzeStmt(s.body);
        }
    }

    private String getExprType(Expr expr) {
        if (expr instanceof LiteralExpr) {
            Object v = ((LiteralExpr) expr).value;
            if (v instanceof Integer) return "INT";
            if (v instanceof Double) return "FLOAT";
            if (v instanceof String) return "STRING";
            return "UNKNOWN";
        }

        if (expr instanceof VariableExpr) {
            String name = ((VariableExpr) expr).name.lexeme;
            if (!symbolTable.containsKey(name)) {
                throw new RuntimeException("Erro Semântico: Variável '" + name + "' não definida.");
            }
            return symbolTable.get(name);
        }

        if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            String left = getExprType(b.left);
            String right = getExprType(b.right);

            // Regras Aritméticas
            if (b.operator.type == TokenType.PLUS || b.operator.type == TokenType.MINUS ||
                b.operator.type == TokenType.MUL || b.operator.type == TokenType.DIV) {
                
                if (left.equals("STRING") || right.equals("STRING")) {
                    // Python permite String * Int, mas vamos bloquear para simplificar ou permitir soma de strings
                    if (b.operator.type == TokenType.PLUS && left.equals("STRING") && right.equals("STRING")) {
                        return "STRING"; // Concatenação
                    }
                    throw new RuntimeException("Erro Semântico: Operação aritmética inválida com STRING.");
                }
                
                if (left.equals("FLOAT") || right.equals("FLOAT")) return "FLOAT";
                return "INT";
            }

            // Regras Relacionais
            if (b.operator.type == TokenType.GT || b.operator.type == TokenType.LT || 
                b.operator.type == TokenType.EQ) {
                if (!left.equals(right) && !(isNumber(left) && isNumber(right))) {
                     throw new RuntimeException("Erro Semântico: Comparação incompatível entre " + left + " e " + right);
                }
                return "BOOLEAN";
            }
        }
        
        if (expr instanceof CallExpr) {
            CallExpr c = (CallExpr) expr;
            TokenType type = c.callee.type;
            
            if (type == TokenType.INPUT) return "STRING"; // input() retorna string
            if (type == TokenType.INT) return "INT";      // int() retorna int
            if (type == TokenType.FLOAT) return "FLOAT";  // float() retorna float
        }

        return "UNKNOWN";
    }
    
    private boolean isNumber(String type) {
        return type.equals("INT") || type.equals("FLOAT");
    }
}