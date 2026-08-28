grammar Ds2;

@header {
package name.jurgenei.parsers;
}

program
    : statement* EOF
    ;

statement
    : procDs2Block
    | assignmentStatement
    | methodCallStatement
    | runStatement
    | emptyStatement
    ;

emptyStatement
    : SEMI
    ;

procDs2Block
    : PROC DS2 SEMI ds2Block* RUN SEMI QUIT SEMI
    ;

ds2Block
    : dataBlock
    | packageBlock
    | threadBlock
    | emptyStatement
    ;

dataBlock
    : DATA identifier SEMI ds2Statement* ENDDATA SEMI
    ;

packageBlock
    : PACKAGE identifier SEMI ds2Statement* ENDPACKAGE SEMI
    ;

threadBlock
    : THREAD identifier SEMI ds2Statement* ENDTHREAD SEMI
    ;

ds2Statement
    : declarationStatement
    | methodDecl
    | setFromStatement
    | assignmentStatement
    | methodCallStatement
    | runStatement
    | emptyStatement
    ;

declarationStatement
    : (DCL | DECLARE) identifier identifier SEMI
    ;

methodDecl
    : METHOD methodName LPAREN methodParameterList? RPAREN SEMI methodBody ENDMETHOD SEMI
    ;

methodParameterList
    : methodParameter (COMMA methodParameter)*
    ;

methodParameter
    : identifier identifier
    ;

methodName
    : identifier
    | RUN
    ;

methodBody
    : ds2MethodStatement*
    ;

ds2MethodStatement
    : assignmentStatement
    | methodCallStatement
    | ifStatement
    | returnStatement
    | runStatement
    | emptyStatement
    ;

ifStatement
    : IF condition THEN ds2MethodStatement
    ;

returnStatement
    : RETURN SEMI
    ;

assignmentStatement
    : identifier EQ expression SEMI
    ;

methodCallStatement
    : identifier DOT identifier LPAREN argumentList? RPAREN SEMI
    | identifier LPAREN argumentList? RPAREN SEMI
    ;

setFromStatement
    : SET FROM identifier SEMI
    ;

argumentList
    : expression (COMMA expression)*
    ;

runStatement
    : RUN SEMI
    ;

condition
    : expression comparator expression
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

PROC: P R O C;
DS2: D S '2';
DATA: D A T A;
ENDDATA: E N D D A T A;
METHOD: M E T H O D;
ENDMETHOD: E N D M E T H O D;
RUN: R U N;
QUIT: Q U I T;
DCL: D C L;
DECLARE: D E C L A R E;
IF: I F;
THEN: T H E N;
RETURN: R E T U R N;
PACKAGE: P A C K A G E;
ENDPACKAGE: E N D P A C K A G E;
THREAD: T H R E A D;
ENDTHREAD: E N D T H R E A D;
SET: S E T;
FROM: F R O M;

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




