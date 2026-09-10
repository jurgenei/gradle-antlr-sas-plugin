lexer grammar SasLexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

AND: 'AND';
AS: 'AS';
BY: 'BY';
CASE: 'CASE';
CREATE: 'CREATE';
DATA: 'DATA';
DISTINCT: 'DISTINCT';
DO: 'DO';
DROP: 'DROP';
ELSE: 'ELSE';
END: 'END';
FORMAT: 'FORMAT';
FROM: 'FROM';
FULL: 'FULL';
GROUP: 'GROUP';
IF: 'IF';
IN: 'IN';
INNER: 'INNER';
JOIN: 'JOIN';
KEEP: 'KEEP';
LEFT: 'LEFT';
MACRO: 'MACRO';
MEND: 'MEND';
NE: 'NE';
NOT: 'NOT';
ON: 'ON';
OR: 'OR';
ORDER: 'ORDER';
OUT: 'OUT';
PROC: 'PROC';
QUIT: 'QUIT';
RIGHT: 'RIGHT';
RUN: 'RUN';
SELECT: 'SELECT';
SET: 'SET';
SQL: 'SQL';
TABLE: 'TABLE';
THEN: 'THEN';
TO: 'TO';
WHEN: 'WHEN';
WHERE: 'WHERE';

STAR: '*';
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
SLASH: '/';
LPAREN: '(';
RPAREN: ')';
LBRACK: '[';
RBRACK: ']';
DOLLAR: '$';
COLON: ':';
PERCENT: '%';
AMP: '&';

NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' ('\'\'' | ~'\'')* '\'';
DQ_STRING: '"' ('\\"' | ~["\r\n])* '"';
ID: [A-Z_][A-Z0-9_]*;

BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;
