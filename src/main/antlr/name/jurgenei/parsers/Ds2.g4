grammar Ds2;

@header {
package name.jurgenei.parsers;
}

program
    : statement* EOF
    ;

statement
    : procDs2Block
    | packageBlock
    | dataBlock
    | threadBlock
    | declarationStatement
    | methodDecl
    | assignmentStatement
    | methodCallStatement
    | runStatement
    | gotoStatement
    | labelStatement
    | ifStatement
    | doBlock
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
    : PACKAGE packageName packageOption* SEMI ds2Statement* ENDPACKAGE SEMI
    ;

packageName
    : identifier
    | stringLiteral
    ;

packageOption
    : SLASH identifier
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
    | gotoStatement
    | labelStatement
    | ifStatement
    | doBlock
    | emptyStatement
    ;

declarationStatement
    : (DCL | DECLARE) declarationType declarationItem+ SEMI
    ;

declarationType
    : PACKAGE identifier
    | identifier typeArguments?
    ;

declarationItem
    : identifier arraySuffix? constructorCall?
    ;

arraySuffix
    : LBRACK expression? RBRACK
    ;

constructorCall
    : LPAREN argumentList? RPAREN
    ;

typeArguments
    : LPAREN expression (COMMA expression)* RPAREN
    ;

methodDecl
    : METHOD methodName LPAREN methodParameterList? RPAREN SEMI methodBody ENDMETHOD SEMI
    | METHOD methodName LPAREN methodParameterList? RPAREN SEMI methodBody END SEMI
    ;

methodParameterList
    : methodParameter (COMMA methodParameter)*
    ;

methodParameter
    : parameterQualifier* declarationType identifier
    ;

parameterQualifier
    : IN
    | OUT
    | IN_OUT
    ;

methodName
    : identifier
    | RUN
    ;

methodBody
    : ds2MethodStatement*
    ;

ds2MethodStatement
    : declarationStatement
    | assignmentStatement
    | methodCallStatement
    | ifStatement
    | doBlock
    | gotoStatement
    | labelStatement
    | returnStatement
    | runStatement
    | emptyStatement
    ;

ifStatement
    : IF expression THEN (doBlock | ds2MethodStatement) (ELSE (doBlock | ds2MethodStatement))?
    ;

doBlock
    : DO doHeader? SEMI? ds2MethodStatement* END SEMI
    ;

doHeader
    : WHILE LPAREN expression RPAREN
    | identifier EQ expression TO expression
    ;

returnStatement
    : RETURN expression? SEMI
    ;

assignmentStatement
    : assignable (EQ | ASSIGN) expression SEMI
    ;

assignable
    : identifier
    | identifier LBRACK expression RBRACK
    ;

methodCallStatement
    : identifier DOT identifier LPAREN argumentList? RPAREN SEMI
    | identifier LPAREN argumentList? RPAREN SEMI
    ;

gotoStatement
    : GOTO identifier SEMI
    ;

labelStatement
    : identifier COLON
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
    : logicalOrExpression
    ;

logicalOrExpression
    : logicalAndExpression (OR logicalAndExpression)*
    ;

logicalAndExpression
    : comparisonExpression (AND comparisonExpression)*
    ;

comparisonExpression
    : concatenationExpression (comparator concatenationExpression)?
    | concatenationExpression (NOT? IN LPAREN expression (COMMA expression)* RPAREN)
    ;

concatenationExpression
    : additiveExpression (CONCAT additiveExpression)*
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
    | arrayAccess
    | LPAREN expression RPAREN
    ;

functionCall
    : identifier LPAREN argumentList? RPAREN
    ;

arrayAccess
    : identifier LBRACK expression RBRACK
    ;

literal
    : NUMBER
    | stringLiteral
    | NULL_LITERAL
    ;

stringLiteral
    : STRING
    | DQ_STRING
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
DO: D O;
END: E N D;
ELSE: E L S E;
TO: T O;
IN: I N;
NOT: N O T;
GOTO: G O T O;
WHILE: W H I L E;
OUT: O U T;
IN_OUT: I N '_' O U T;
OR: O R;
AND: A N D;
NULL_LITERAL: N U L L;

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
