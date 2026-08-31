lexer grammar CaslLexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

RUN: 'RUN';
IF: 'IF';
THEN: 'THEN';
AND: 'AND';
OR: 'OR';
TRUE: 'TRUE';
FALSE: 'FALSE';
NULL: 'NULL';

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

