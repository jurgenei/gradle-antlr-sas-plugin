parser grammar CaslParser;

@header {
package name.jurgenei.parsers;
}

options { tokenVocab = CaslLexer; }

program
    : statement* EOF
    ;

statement
    : assignmentStatement
    | actionRunStatement
    | runStatement
    | ifStatement
    | emptyStatement
    ;

emptyStatement
    : SEMI
    ;

assignmentStatement
    : identifier EQ expression SEMI
    ;

actionRunStatement
    : identifier DOT identifier SLASH argumentList? SEMI
    ;

argumentList
    : argument (COMMA argument)*
    ;

argument
    : identifier EQ expression
    ;

runStatement
    : RUN SEMI
    ;

ifStatement
    : IF condition THEN statement
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
    | arrayLiteral
    | objectLiteral
    | identifier
    | LPAREN expression RPAREN
    ;

arrayLiteral
    : LBRACK (expression (COMMA expression)*)? RBRACK
    ;

objectLiteral
    : LBRACE (objectEntry (COMMA objectEntry)*)? RBRACE
    ;

objectEntry
    : (identifier | STRING) EQ expression
    ;

literal
    : NUMBER
    | STRING
    | TRUE
    | FALSE
    | NULL
    ;

identifier
    : ID
    ;

