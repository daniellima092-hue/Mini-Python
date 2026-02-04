package br.ifs.compiladores;

public class TACInstruction {
    public final String op;     // Operador (ADD, SUB, IF, GOTO, PARAM, CALL...)
    public final String arg1;   // Primeiro operando (ou null)
    public final String arg2;   // Segundo operando (ou null)
    public final String result; // Onde o resultado é armazenado (ou label de destino)

    public TACInstruction(String result, String arg1, String op, String arg2) {
        this.result = result;
        this.arg1 = arg1;
        this.op = op;
        this.arg2 = arg2;
    }

    // Construtor para instruções de cópia ou unárias (x = y)
    public TACInstruction(String result, String arg1) {
        this(result, arg1, "", null);
    }
    
    // Construtor para Labels (L1:)
    public TACInstruction(String label) {
        this(label, null, "LABEL", null);
    }

    @Override
    public String toString() {
        if (op.equals("LABEL")) {
            return result + ":";
        }
        if (op.equals("GOTO")) {
            return "goto " + result;
        }
        if (op.equals("IF_FALSE")) {
            return "ifFalse " + arg1 + " goto " + result;
        }
        if (op.equals("PRINT")) {
            return "print " + arg1;
        }
        if (op.equals("CALL")) {
            // Ex: t1 = call func, n
            return result + " = call " + arg1 + ", " + arg2;
        }
        if (arg2 != null) {
            // Formato: t1 = a + b
            return result + " = " + arg1 + " " + op + " " + arg2;
        }
        if (!op.isEmpty()) {
             // Formato unário: t1 = - a
            return result + " = " + op + " " + arg1;
        }
        // Formato cópia: x = y
        return result + " = " + arg1;
    }
}
