lexer grammar FedSqlLexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

AND: 'AND';
AS: 'AS';
FROM: 'FROM';
INNER: 'INNER';
JOIN: 'JOIN';
LEFT: 'LEFT';
ON: 'ON';
OR: 'OR';
RIGHT: 'RIGHT';
SELECT: 'SELECT';
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

NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' ('\'\'' | ~'\'')* '\'';
ID: [A-Z_][A-Z0-9_]*;

BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;

