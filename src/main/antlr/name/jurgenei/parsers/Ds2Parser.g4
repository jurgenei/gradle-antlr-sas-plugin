parser grammar Ds2Parser;

@header {
package name.jurgenei.parsers;
}

options { tokenVocab = Ds2Lexer; }

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

