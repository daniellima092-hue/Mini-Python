# Mini-Python Compiler

Projeto de construção de um compilador para a linguagem Mini-Python, desenvolvido para a disciplina de Compiladores do Instituto Federal de Sergipe (IFS).

## Membros do Grupo
- Daniel Santos Lima
- Davi Costa Martins
- Rafael Barros Santos

## Funcionalidades Implementadas

### 1. Analisador Léxico (Scanner)
- Suporte a indentação significativa (Tokens `INDENT`/`DEDENT`).
- Identificação de palavras reservadas (`if`, `else`, `while`, `print`, etc.).
- Tratamento de literais (Inteiros, Floats, Strings).

### 2. Analisador Sintático (Parser)
- Análise Descendente Recursiva.
- Geração da Árvore Sintática Abstrata (AST).
- Suporte a expressões aritméticas e lógicas com precedência correta.

### 3. Análise Semântica
- Verificação básica de tipos e declarações.

### 4. Gerador de Código Intermediário (TAC)
- Tradução da AST para **Código de Três Endereços** (Three-Address Code).
- Linearização de estruturas de controle (`if`, `while`) utilizando *Labels* (`L1`, `L2`) e *Saltos* (`goto`, `ifFalse`).
- Uso de variáveis temporárias (`t1`, `t2`...) para decompor expressões complexas.

## Requisitos
- Java 17+
- Maven
- IDE Java (VS Code, IntelliJ ou Eclipse)

## Como Executar

### Rodar os Testes Unitários
Para verificar se o analisador léxico e outras unidades estão funcionando conforme esperado:

```bash
mvn test