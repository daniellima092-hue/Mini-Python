package br.ifs.compiladores;

import java.util.List;

// Classe base para declarações (comandos)
abstract class Stmt { }

// Classe base para expressões (valores)
abstract class Expr { }

// --- Declarações (Statements) ---

class BlockStmt extends Stmt {
    final List<Stmt> statements;
    BlockStmt(List<Stmt> statements) { this.statements = statements; }
}

class IfStmt extends Stmt {
    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;

    IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}

class WhileStmt extends Stmt {
    final Expr condition;
    final Stmt body;

    WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }
}

class AssignStmt extends Stmt {
    final Token name;
    final Expr value;

    AssignStmt(Token name, Expr value) {
        this.name = name;
        this.value = value;
    }
}

class PrintStmt extends Stmt {
    final Expr expression;
    PrintStmt(Expr expression) { this.expression = expression; }
}

// --- Expressões (Expressions) ---

class BinaryExpr extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    BinaryExpr(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}

class UnaryExpr extends Expr {
    final Token operator;
    final Expr right;
    UnaryExpr(Token operator, Expr right) {
        this.operator = operator;
        this.right = right;
    }
}

class LiteralExpr extends Expr {
    final Object value;
    LiteralExpr(Object value) { this.value = value; }
}

class VariableExpr extends Expr {
    final Token name;
    VariableExpr(Token name) { this.name = name; }
}

class CallExpr extends Expr {
    final Token callee; // input, int, float
    final List<Expr> arguments;
    CallExpr(Token callee, List<Expr> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }
}