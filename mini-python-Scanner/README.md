# Mini-Python Compiler

Implementação da Entrega 2 (Analisador Léxico).

## Membros do Grupo
- Daniel Santos Lima
- Davi Costa Martins
- Rafael Barros Santos

## Estrutura
O projeto segue a gramática Mini-Python definida. O Lexer suporta:
- Indentação significativa (Geração de tokens INDENT/DEDENT).
- Operadores aritméticos (+, -, *, /) e relacionais (==, !=, >, <, >=, <=).
- Tipos de dados: Inteiros, Floats, Strings.
- Palavras reservadas: if, else, while, print, and, or, not.

## Requisitos
- Java 17+
- Maven

## Como Executar os Testes
Para verificar a tokenização correta dos exemplos da gramática:

```bash
mvn test