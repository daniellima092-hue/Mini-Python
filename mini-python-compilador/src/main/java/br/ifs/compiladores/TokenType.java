package br.ifs.compiladores;

public enum TokenType {
    // Palavras Reservadas e Built-ins
    IF, ELSE, WHILE, PRINT,
    AND, OR, NOT,
    INPUT, INT, FLOAT, // Tratados como tokens específicos para facilitar o parser

    // Identificadores e Literais
    IDENTIFIER,
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,

    // Operadores Aritméticos
    PLUS, MINUS, MUL, DIV,  // +, -, *, /

    // Operadores Relacionais
    GT, LT, EQ, GTE, LTE, NEQ, // >, <, ==, >=, <=, !=

    // Atribuição e Pontuação
    ASSIGN,     // =
    LPAREN,     // (
    RPAREN,     // )
    COLON,      // :
    COMMA,      // ,

    // Controle de Estrutura (Indentação Significativa)
    NEWLINE,
    INDENT,
    DEDENT,

    // Especiais
    EOF,
    ERROR, TRUE, FALSE
}
