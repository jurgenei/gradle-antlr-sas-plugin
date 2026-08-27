grammar Sas;

@header {
package name.jurgenei.parsers;
}

program
    : statement* EOF
    ;

statement
    : dataStep
    | procSql
    | emptyStatement
    ;

emptyStatement
    : SEMI
    ;

dataStep
    : DATA identifier dataSetOption* SEMI dataStepBody RUN SEMI
    ;

dataSetOption
    : KEEP EQ identifierList
    | DROP EQ identifierList
    ;

dataStepBody
    : dataStepStatement*
    ;

dataStepStatement
    : setStatement
    | assignmentStatement
    | formatStatement
    | outputStatement
    | ifStatement
    | emptyStatement
    ;

setStatement
    : SET identifier dataSetOption* SEMI
    ;

formatStatement
    : FORMAT formatSpec+ SEMI
    ;

formatSpec
    : identifier formatName
    ;

formatName
    : DOLLAR ID DOT
    | ID (DOT NUMBER)? DOT?
    ;

assignmentStatement
    : identifier EQ expression SEMI
    ;

outputStatement
    : OUTPUT SEMI
    ;

ifStatement
    : IF condition THEN assignmentStatement
    ;

procSql
    : PROC SQL SEMI sqlStatement+ QUIT SEMI
    ;

sqlStatement
    : createTableAsStatement
    | selectStatement SEMI
    ;

createTableAsStatement
    : CREATE TABLE identifier AS selectStatement SEMI?
    ;

selectStatement
    : SELECT selectList FROM fromSource (WHERE condition)?
    ;

fromSource
    : tableRef joinClause*
    ;

joinClause
    : joinType? JOIN tableRef ON condition
    ;

tableRef
    : identifier identifier?
    ;

joinType
    : INNER
    | LEFT
    | RIGHT
    ;

selectList
    : STAR
    | selectItem (COMMA selectItem)*
    ;

selectItem
    : expression (AS identifier)?
    ;

condition
    : booleanTerm (OR booleanTerm)*
    ;

booleanTerm
    : booleanFactor (AND booleanFactor)*
    ;

booleanFactor
    : LPAREN condition RPAREN
    | expression comparator expression
    ;

comparator
    : EQ
    | NEQ
    | LT
    | LTE
    | GT
    | GTE
    ;

expression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
    ;

multiplicativeExpression
    : primaryExpression ((STAR | SLASH) primaryExpression)*
    ;

primaryExpression
    : literal
    | identifier
    | LPAREN expression RPAREN
    ;

literal
    : NUMBER
    | STRING
    ;

identifier
    : ID (DOT ID)*
    ;

identifierList
    : identifier (identifier)*
    ;

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
KEEP: K E E P;
DROP: D R O P;
FORMAT: F O R M A T;
JOIN: J O I N;
LEFT: L E F T;
RIGHT: R I G H T;
INNER: I N N E R;
ON: O N;
AND: A N D;
OR: O R;

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
DOLLAR: '$';

NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' ('\'\'' | ~'\'')* '\'';
ID: [A-Za-z_][A-Za-z0-9_]*;

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

