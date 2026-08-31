parser grammar SasParser;

@header {
package name.jurgenei.parsers;
}

options { tokenVocab = SasLexer; }

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
