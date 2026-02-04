package br.ifs.compiladores;

import java.util.ArrayList;
import java.util.List;

public class MiniPythonTACGenerator {
    
    // Lista linear de instruções [cite: 76]
    private final List<TACInstruction> code = new ArrayList<>();
    
    // Contadores para temporários e labels
    private int tempCount = 1;
    private int labelCount = 1;

    // --- API Pública ---
    public List<TACInstruction> generate(List<Stmt> statements) {
        code.clear();
        tempCount = 1;
        labelCount = 1;
        
        for (Stmt stmt : statements) {
            genStmt(stmt);
        }
        return code;
    }

    // --- Geração de Declarações (Statements) ---

    private void genStmt(Stmt stmt) {
        if (stmt instanceof BlockStmt) {
            for (Stmt s : ((BlockStmt) stmt).statements) {
                genStmt(s);
            }
        } 
        else if (stmt instanceof AssignStmt) {
            AssignStmt s = (AssignStmt) stmt;
            String exprTemp = genExpr(s.value);
            // x = t1
            emit(new TACInstruction(s.name.lexeme, exprTemp));
        } 
        else if (stmt instanceof PrintStmt) {
            PrintStmt s = (PrintStmt) stmt;
            String exprTemp = genExpr(s.expression);
            // print t1
            emit(new TACInstruction(null, exprTemp, "PRINT", null));
        } 
        else if (stmt instanceof IfStmt) {
            genIfStmt((IfStmt) stmt);
        } 
        else if (stmt instanceof WhileStmt) {
            genWhileStmt((WhileStmt) stmt);
        }
    }

    private void genIfStmt(IfStmt stmt) {
        String labelElse = newLabel();
        String labelEnd = newLabel();

        // 1. Avalia condição
        String condTemp = genExpr(stmt.condition);
        
        // 2. Se falso, pula para o Else (ou fim)
        emit(new TACInstruction(labelElse, condTemp, "IF_FALSE", null));
        
        // 3. Bloco Then
        genStmt(stmt.thenBranch);
        emit(new TACInstruction(labelEnd, null, "GOTO", null)); // Pula o else ao terminar o then

        // 4. Label Else
        emit(new TACInstruction(labelElse));
        if (stmt.elseBranch != null) {
            genStmt(stmt.elseBranch);
        }

        // 5. Label Fim
        emit(new TACInstruction(labelEnd));
    }

    private void genWhileStmt(WhileStmt stmt) {
        String labelStart = newLabel();
        String labelEnd = newLabel();

        // 1. Label de início (para o loop voltar)
        emit(new TACInstruction(labelStart));

        // 2. Avalia condição
        String condTemp = genExpr(stmt.condition);

        // 3. Se falso, sai do loop
        emit(new TACInstruction(labelEnd, condTemp, "IF_FALSE", null));

        // 4. Corpo do Loop
        genStmt(stmt.body);

        // 5. Volta para o início
        emit(new TACInstruction(labelStart, null, "GOTO", null));

        // 6. Label de fim
        emit(new TACInstruction(labelEnd));
    }

    // --- Geração de Expressões ---

    private String genExpr(Expr expr) {
        if (expr instanceof LiteralExpr) {
            // Retorna o valor direto como string (ex: "10", "5.5")
            return String.valueOf(((LiteralExpr) expr).value);
        }

        if (expr instanceof VariableExpr) {
            // Retorna o nome da variável (ex: "x")
            return ((VariableExpr) expr).name.lexeme;
        }

        if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            String t1 = genExpr(b.left);
            String t2 = genExpr(b.right);
            String temp = newTemp();
            
            String op = getOperatorSymbol(b.operator.type);
            
            // t3 = t1 + t2 
            emit(new TACInstruction(temp, t1, op, t2));
            return temp;
        }

        if (expr instanceof UnaryExpr) {
            UnaryExpr u = (UnaryExpr) expr;
            String t1 = genExpr(u.right);
            String temp = newTemp();
            
            String op = getOperatorSymbol(u.operator.type);
            
            // t2 = - t1
            emit(new TACInstruction(temp, t1, op, null));
            return temp;
        }

        if (expr instanceof CallExpr) {
            CallExpr c = (CallExpr) expr;
            String funcName = c.callee.lexeme; // input, int, float
            
            // Avalia argumentos (simplificação: assume-se poucos argumentos para built-ins)
            // Python real empilharia args, aqui vamos simplificar para "call func, num_args"
            for (Expr arg : c.arguments) {
                String t = genExpr(arg);
                emit(new TACInstruction(null, t, "PARAM", null)); // param t1
            }
            
            String temp = newTemp();
            // t1 = call input, 0
            emit(new TACInstruction(temp, funcName, "CALL", String.valueOf(c.arguments.size())));
            return temp;
        }

        throw new RuntimeException("Expressão não suportada para TAC.");
    }

    // --- Helpers ---

    private void emit(TACInstruction instr) {
        code.add(instr);
    }

    private String newTemp() {
        return "t" + (tempCount++);
    }

    private String newLabel() {
        return "L" + (labelCount++);
    }

    private String getOperatorSymbol(TokenType type) {
        switch (type) {
            case PLUS: return "+";
            case MINUS: return "-";
            case MUL: return "*";
            case DIV: return "/";
            case GT: return ">";
            case LT: return "<";
            case GTE: return ">=";
            case LTE: return "<=";
            case EQ: return "==";
            case NEQ: return "!=";
            case AND: return "&&";
            case OR: return "||";
            case NOT: return "!";
            default: return "?";
        }
    }
}
