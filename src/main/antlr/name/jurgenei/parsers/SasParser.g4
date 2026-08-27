parser grammar SasParser;

@header {
package name.jurgenei.parsers;
}

options { tokenVocab = SasLexer; }

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
    : DATA identifier SEMI dataStepBody RUN SEMI
    ;

dataStepBody
    : dataStepStatement*
    ;

dataStepStatement
    : setStatement
    | assignmentStatement
    | outputStatement
    | ifStatement
    | emptyStatement
    ;

setStatement
    : SET identifier SEMI
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
    : SELECT selectList FROM identifier (WHERE condition)?
    ;

selectList
    : STAR
    | identifier (COMMA identifier)*
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
    : literal
    | identifier
    ;

literal
    : NUMBER
    | STRING
    ;

identifier
    : ID (DOT ID)*
    ;

