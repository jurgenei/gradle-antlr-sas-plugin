grammar Sas;

@header {
package name.jurgenei.parsers;
}

program
    : statement* EOF
    ;

statement
    : macroDefinition
    | macroInvocation
    | dataStep
    | procSql
    | genericStatement
    | emptyStatement
    ;

emptyStatement
    : SEMI
    ;

macroDefinition
    : PERCENT MACRO ID LPAREN macroParamList? RPAREN SEMI statement* PERCENT MEND SEMI
    ;

macroParamList
    : ID (COMMA ID)*
    ;

macroInvocation
    : PERCENT ID LPAREN argumentList? RPAREN SEMI?
    ;

dataStep
    : DATA identifier dataSetOption* SEMI dataStepBody RUN SEMI
    ;

dataSetOption
    : KEEP EQ identifierList
    | DROP EQ identifierList
    | LPAREN dataSetOption (dataSetOption)* RPAREN
    ;

dataStepBody
    : dataStepStatement*
    ;

dataStepStatement
    : setStatement
    | assignmentStatement
    | formatStatement
    | ifStatement
    | doBlock
    | genericStatement
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
    : assignable EQ expression SEMI
    ;

assignable
    : identifier
    | identifier LBRACK expression RBRACK
    ;

ifStatement
    : IF condition (THEN (assignmentStatement | doBlock | genericStatement))? (ELSE (assignmentStatement | doBlock | genericStatement))? SEMI?
    ;

doBlock
    : DO doHeader? SEMI? dataStepStatement* END SEMI
    ;

doHeader
    : identifier EQ expression TO expression
    ;

procSql
    : PROC SQL SEMI procSqlStatement* QUIT SEMI
    ;

procSqlStatement
    : createTableAsStatement
    | selectStatement SEMI
    | genericSqlStatement
    | emptyStatement
    ;

createTableAsStatement
    : CREATE TABLE identifier AS selectStatement SEMI?
    ;

selectStatement
    : SELECT DISTINCT? selectList FROM fromSource (WHERE condition)?
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
    | FULL
    ;

selectList
    : STAR
    | selectItem (COMMA selectItem)*
    ;

selectItem
    : caseExpression (AS identifier)?
    | expression (AS identifier)?
    ;

caseExpression
    : CASE whenClause+ (ELSE expression)? END
    ;

whenClause
    : WHEN condition THEN expression
    ;

genericSqlStatement
    : sqlToken+ SEMI
    ;

sqlToken
    : ID
    | NUMBER
    | stringLiteral
    | LPAREN
    | RPAREN
    | COMMA
    | DOT
    | EQ
    | NEQ
    | LT
    | LTE
    | GT
    | GTE
    | PLUS
    | MINUS
    | STAR
    | SLASH
    | DOLLAR
    | SELECT
    | FROM
    | WHERE
    | AS
    | JOIN
    | LEFT
    | RIGHT
    | FULL
    | INNER
    | ON
    | ORDER
    | BY
    | GROUP
    | DISTINCT
    | AND
    | OR
    | NOT
    | IN
    | CASE
    | WHEN
    | THEN
    | ELSE
    | END
    ;

genericStatement
    : genericToken+ SEMI
    ;

genericToken
    : ID
    | NUMBER
    | stringLiteral
    | LPAREN
    | RPAREN
    | LBRACK
    | RBRACK
    | COMMA
    | DOT
    | EQ
    | NEQ
    | LT
    | LTE
    | GT
    | GTE
    | PLUS
    | MINUS
    | STAR
    | SLASH
    | DOLLAR
    | COLON
    | PERCENT
    | AMP
    | AS
    | CASE
    | WHEN
    | THEN
    | ELSE
    | END
    | ORDER
    | BY
    | GROUP
    | DISTINCT
    | AND
    | OR
    | NOT
    | IN
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
    | expression (NOT? IN LPAREN expression (COMMA expression)* RPAREN)
    | expression
    ;

comparator
    : EQ
    | NEQ
    | LT
    | LTE
    | GT
    | GTE
    | NE
    | IN
    | NOT IN
    ;

expression
    : logicalOrExpression
    ;

logicalOrExpression
    : logicalAndExpression (OR logicalAndExpression)*
    ;

logicalAndExpression
    : comparisonExpression (AND comparisonExpression)*
    ;

comparisonExpression
    : additiveExpression (comparator additiveExpression)?
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
    | functionCall
    | LPAREN expression RPAREN
    ;

functionCall
    : identifier LPAREN argumentList? RPAREN
    ;

argumentList
    : expression (COMMA expression)*
    ;

literal
    : NUMBER
    | stringLiteral
    | DOT
    ;

stringLiteral
    : STRING
    | DQ_STRING
    ;

identifier
    : identifierPart (DOT identifierPart)*
    ;

identifierPart
    : ID
    | IN
    | OUT
    | DATA
    | SET
    | RUN
    | PROC
    | SQL
    | TABLE
    | SELECT
    | FROM
    | WHERE
    | AS
    | IF
    | THEN
    | ELSE
    | DO
    | END
    ;

identifierList
    : identifier+
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
IF: I F;
THEN: T H E N;
ELSE: E L S E;
DO: D O;
END: E N D;
TO: T O;
KEEP: K E E P;
DROP: D R O P;
FORMAT: F O R M A T;
JOIN: J O I N;
LEFT: L E F T;
RIGHT: R I G H T;
FULL: F U L L;
INNER: I N N E R;
ON: O N;
AND: A N D;
OR: O R;
NOT: N O T;
IN: I N;
OUT: O U T;
NE: N E;
CASE: C A S E;
WHEN: W H E N;
ORDER: O R D E R;
BY: B Y;
GROUP: G R O U P;
DISTINCT: D I S T I N C T;
MACRO: M A C R O;
MEND: M E N D;

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
