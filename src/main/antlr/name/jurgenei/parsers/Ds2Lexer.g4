lexer grammar Ds2Lexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

PROC: 'PROC';
DS2: 'DS2';
DATA: 'DATA';
ENDDATA: 'ENDDATA';
METHOD: 'METHOD';
ENDMETHOD: 'ENDMETHOD';
RUN: 'RUN';
QUIT: 'QUIT';
DCL: 'DCL';
DECLARE: 'DECLARE';
IF: 'IF';
THEN: 'THEN';
RETURN: 'RETURN';
PACKAGE: 'PACKAGE';
ENDPACKAGE: 'ENDPACKAGE';
THREAD: 'THREAD';
ENDTHREAD: 'ENDTHREAD';
SET: 'SET';
FROM: 'FROM';
DO: 'DO';
END: 'END';
ELSE: 'ELSE';
TO: 'TO';
IN: 'IN';
NOT: 'NOT';
GOTO: 'GOTO';
WHILE: 'WHILE';
OUT: 'OUT';
IN_OUT: 'IN_OUT';
OR: 'OR';
AND: 'AND';
NULL_LITERAL: 'NULL';

LPAREN: '(';
RPAREN: ')';
COMMA: ',';
SEMI: ';';
DOT: '.';
EQ: '=';
ASSIGN: ':=';
NEQ: '^=' | '!=';
LTE: '<=';
GTE: '>=';
LT: '<';
GT: '>';
COLON: ':';
PLUS: '+';
MINUS: '-';
STAR: '*';
SLASH: '/';
CONCAT: '||';
LBRACK: '[';
RBRACK: ']';

NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' ('\'\'' | ~'\'')* '\'';
DQ_STRING: '"' ('\\"' | ~["\r\n])* '"';
ID: [A-Z_][A-Z0-9_]*;

BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;

