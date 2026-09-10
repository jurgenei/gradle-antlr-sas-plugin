lexer grammar Ds2Lexer;

@header {
package name.jurgenei.parsers;
}

options { caseInsensitive = true; }

AND: 'AND';
DATA: 'DATA';
DCL: 'DCL';
DECLARE: 'DECLARE';
DO: 'DO';
DS2: 'DS2';
ELSE: 'ELSE';
END: 'END';
ENDDATA: 'ENDDATA';
ENDMETHOD: 'ENDMETHOD';
ENDPACKAGE: 'ENDPACKAGE';
ENDTHREAD: 'ENDTHREAD';
FROM: 'FROM';
GOTO: 'GOTO';
IF: 'IF';
IN: 'IN';
IN_OUT: 'IN_OUT';
METHOD: 'METHOD';
NOT: 'NOT';
NULL_LITERAL: 'NULL';
OR: 'OR';
OUT: 'OUT';
PACKAGE: 'PACKAGE';
PROC: 'PROC';
QUIT: 'QUIT';
RETURN: 'RETURN';
RUN: 'RUN';
SET: 'SET';
THEN: 'THEN';
THREAD: 'THREAD';
TO: 'TO';
WHILE: 'WHILE';

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

