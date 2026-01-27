package br.ifs.compiladores;

public enum TokenType {
    // Palavras Reservadas e Built-ins [cite: 51, 87-89, 18-23]
    IF, ELSE, WHILE, PRINT,
    AND, OR, NOT,
    INPUT, INT, FLOAT, // Tratados como tokens específicos para facilitar o parser

    // Identificadores e Literais [cite: 75-80]
    IDENTIFIER,
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,

    // Operadores Aritméticos [cite: 41, 43]
    PLUS, MINUS, MUL, DIV,  // +, -, *, /

    // Operadores Relacionais [cite: 39]
    GT, LT, EQ, GTE, LTE, NEQ, // >, <, ==, >=, <=, !=

    // Atribuição e Pontuação [cite: 14, 20]
    ASSIGN,     // =
    LPAREN,     // (
    RPAREN,     // )
    COLON,      // :
    COMMA,      // ,

    // Controle de Estrutura (Indentação Significativa) [cite: 81-84]
    NEWLINE,
    INDENT,
    DEDENT,

    // Especiais
    EOF,
    ERROR
}