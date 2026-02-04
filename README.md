Compilador Mini-Python 🐍
Este projeto documenta a jornada de criação de um compilador completo para a linguagem Mini-Python. O sistema transforma código de alto nível em uma representação linear de baixo nível (TAC).

🚀 Ciclo de Vida do Projeto
O compilador foi construído seguindo rigorosamente as definições de gramática e estrutura estabelecidas no início do desenvolvimento.

1. Especificação e Gramática (Entrega 1)
Objetivo: Definir as regras sintáticas e o vocabulário da linguagem.

O que foi feito: Definição da gramática usando a notação BNF (Backus-Naur Form). Aqui foram decididas as palavras reservadas (if, while, print), os operadores e a estrutura de blocos baseada em indentação.

Impacto no Projeto: Esta fase serviu como o "contrato". Se o PDF da gramática diz que um if precisa de :, o código da Entrega 3 (Parser) foi programado exatamente para procurar esse símbolo.

2. Análise Léxica (Entrega 2)
Objetivo: Converter o texto bruto em Tokens.

Como funciona: O MiniPythonLexer lê caractere por caractere. Ele agrupa p, r, i, n, t no token PRINT.

Diferencial: Implementação da Pilha de Indentação. Ela gera os tokens virtuais INDENT e DEDENT que permitem ao Parser identificar o início e o fim de blocos sem o uso de chaves {}.

3. Análise Sintática (Entrega 3)
Objetivo: Construir a Árvore de Sintaxe Abstrata (AST).

Arquivos: MiniPythonParser.java e AstNodes.java.

Como funciona: O Parser consome os tokens e os organiza em objetos hierárquicos. Por exemplo, uma atribuição vira um objeto AssignStmt, que guarda o nome da variável e o valor associado.

4. Análise Semântica e TAC (Entrega 4)
Objetivo: Validar a lógica e gerar o código intermediário.

Arquivos: MiniPythonTACGenerator.java e TACInstruction.java.

Semântica: Verifica se as variáveis usadas foram declaradas e se os tipos são compatíveis (ex: não subtrair texto de número).

TAC (Three-Address Code): O gerador percorre a AST e a "achata" em instruções lineares simples, facilitando a tradução final para assembly ou execução.

Exemplo: x = 10 + 5 * 2 vira: t1 = 5 * 2 t2 = 10 + t1 x = t2

🛠️ Como as entregas se complementam?
A Entrega 1 (BNF) forneceu o mapa.

A Entrega 2 (Lexer) forneceu as peças (Tokens).

A Entrega 3 (Parser) montou a estrutura (AST) baseada no mapa da Entrega 1.

A Entrega 4 (Semântica/TAC) validou se a montagem fazia sentido e gerou a lista final de instruções.

🧪 Como Testar
O projeto utiliza testes automatizados para validar cada fase:

Léxico/Sintático: MiniPythonParserTest.java.

Geração de Código: MiniPythonTACTest.java.

Para rodar todos os testes e garantir que o compilador está íntegro:

mvn test
