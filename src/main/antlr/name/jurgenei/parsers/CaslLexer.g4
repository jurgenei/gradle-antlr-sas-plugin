lexer grammar CaslLexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

AND: 'AND';
FALSE: 'FALSE';
IF: 'IF';
NULL: 'NULL';
OR: 'OR';
RUN: 'RUN';
THEN: 'THEN';
TRUE: 'TRUE';

LBRACE: '{';
RBRACE: '}';
LBRACK: '[';
RBRACK: ']';
LPAREN: '(';
RPAREN: ')';
COMMA: ',';
SEMI: ';';
DOT: '.';
EQ: '=';
NEQ: '^=' | '!=';
LTE: '<=';
GTE: '>=';
LT: '<';
GT: '>';
PLUS: '+';
MINUS: '-';
STAR: '*';
SLASH: '/';

NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' ('\'\'' | ~'\'')* '\'';
ID: [A-Z_][A-Z0-9_]*;

BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;

