lexer grammar FedSqlLexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

SELECT: 'SELECT';
FROM: 'FROM';
WHERE: 'WHERE';
AS: 'AS';
JOIN: 'JOIN';
INNER: 'INNER';
LEFT: 'LEFT';
RIGHT: 'RIGHT';
ON: 'ON';
AND: 'AND';
OR: 'OR';

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

