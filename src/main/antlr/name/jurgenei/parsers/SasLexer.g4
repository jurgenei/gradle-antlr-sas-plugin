lexer grammar SasLexer;

@header {
package name.jurgenei.parsers;
}

DATA: D A T A;
SET: S E T;
RUN: R U N;
PROC: P R O C;
SQL: S Q L;
QUIT: Q U I T;
CREATE: C R E A T E;
TABLE: T A B L E;
AS: A S;
SELECT: S E L E C T;
FROM: F R O M;
WHERE: W H E R E;
OUTPUT: O U T P U T;
IF: I F;
THEN: T H E N;

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

NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' ('\'\'' | ~'\'')* '\'';
ID: [A-Za-z_][A-Za-z0-9_]*;

LINE_COMMENT: '*' ~[\r\n]* ';' -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];

