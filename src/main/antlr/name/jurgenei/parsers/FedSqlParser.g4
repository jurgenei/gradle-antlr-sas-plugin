parser grammar FedSqlParser;

@header {
package name.jurgenei.parsers;
}

options { tokenVocab = FedSqlLexer; }

query
    : statement (SEMI statement)* SEMI? EOF
    ;

statement
    : selectStatement
    ;

selectStatement
    : SELECT selectList FROM tableRef joinClause* (WHERE condition)?
    ;

selectList
    : STAR
    | selectItem (COMMA selectItem)*
    ;

selectItem
    : expression (AS identifier)?
    ;

tableRef
    : identifier identifier?
    ;

joinClause
    : joinType? JOIN tableRef ON condition
    ;

joinType
    : INNER
    | LEFT
    | RIGHT
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

