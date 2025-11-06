package com.github.pmouli.rune.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.pmouli.rune.psi.RosettaTokenTypes;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%public
%class RosettaLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

// Whitespace and comments
CRLF=\R
WHITE_SPACE=[\ \n\t\f]
LINE_COMMENT="//"[^\r\n]*
BLOCK_COMMENT="/"\*([^*]|\*+[^*/])*\*+"/"

// Identifiers and literals
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
INTEGER=[0-9]+
STRING=\"([^\\\"\r\n]|\\[^\r\n])*\"
PATTERN=~\"([^\\\"\r\n]|\\[^\r\n])*\"

%%

<YYINITIAL> {
  // Whitespace
  {WHITE_SPACE}+              { return WHITE_SPACE; }
  {CRLF}+                     { return WHITE_SPACE; }

  // Comments
  {LINE_COMMENT}              { return RosettaTokenTypes.COMMENT; }
  {BLOCK_COMMENT}             { return RosettaTokenTypes.COMMENT; }

  // Keywords - Core constructs
  "namespace"                 { return RosettaTokenTypes.NAMESPACE; }
  "type"                      { return RosettaTokenTypes.TYPE; }
  "func"                      { return RosettaTokenTypes.FUNC; }
  "enum"                      { return RosettaTokenTypes.ENUM; }
  "choice"                    { return RosettaTokenTypes.CHOICE; }
  "alias"                     { return RosettaTokenTypes.ALIAS; }
  "annotation"                { return RosettaTokenTypes.ANNOTATION; }
  "scheme"                    { return RosettaTokenTypes.SCHEME; }
  "calculation"               { return RosettaTokenTypes.CALCULATION; }
  "reporting"                 { return RosettaTokenTypes.REPORTING; }

  // Keywords - Attributes and cardinality
  "extends"                   { return RosettaTokenTypes.EXTENDS; }
  "condition"                 { return RosettaTokenTypes.CONDITION; }
  "optional"                  { return RosettaTokenTypes.OPTIONAL; }
  "one-of"                    { return RosettaTokenTypes.ONE_OF; }
  "required"                  { return RosettaTokenTypes.REQUIRED; }

  // Keywords - Expressions and logic
  "if"                        { return RosettaTokenTypes.IF; }
  "then"                      { return RosettaTokenTypes.THEN; }
  "else"                      { return RosettaTokenTypes.ELSE; }
  "and"                       { return RosettaTokenTypes.AND; }
  "or"                        { return RosettaTokenTypes.OR; }
  "not"                       { return RosettaTokenTypes.NOT; }
  "exists"                    { return RosettaTokenTypes.EXISTS; }
  "only"                      { return RosettaTokenTypes.ONLY; }
  "is"                        { return RosettaTokenTypes.IS; }
  "absent"                    { return RosettaTokenTypes.ABSENT; }

  // Keywords - Built-in types
  "string"                    { return RosettaTokenTypes.STRING_TYPE; }
  "int"                       { return RosettaTokenTypes.INT_TYPE; }
  "number"                    { return RosettaTokenTypes.NUMBER_TYPE; }
  "boolean"                   { return RosettaTokenTypes.BOOLEAN_TYPE; }
  "date"                      { return RosettaTokenTypes.DATE_TYPE; }
  "time"                      { return RosettaTokenTypes.TIME_TYPE; }
  "dateTime"                  { return RosettaTokenTypes.DATETIME_TYPE; }
  "zonedDateTime"             { return RosettaTokenTypes.ZONEDDATETIME_TYPE; }

  // Keywords - Metadata and validation
  "synonym"                   { return RosettaTokenTypes.SYNONYM; }
  "metadata"                  { return RosettaTokenTypes.METADATA; }
  "reference"                 { return RosettaTokenTypes.REFERENCE; }
  "scheme"                    { return RosettaTokenTypes.SCHEME; }
  "id"                        { return RosettaTokenTypes.ID; }
  "key"                       { return RosettaTokenTypes.KEY; }

  // Operators and punctuation
  ":"                         { return RosettaTokenTypes.COLON; }
  ";"                         { return RosettaTokenTypes.SEMICOLON; }
  ","                         { return RosettaTokenTypes.COMMA; }
  "."                         { return RosettaTokenTypes.DOT; }
  "="                         { return RosettaTokenTypes.EQUALS; }
  "("                         { return RosettaTokenTypes.LPAREN; }
  ")"                         { return RosettaTokenTypes.RPAREN; }
  "["                         { return RosettaTokenTypes.LBRACKET; }
  "]"                         { return RosettaTokenTypes.RBRACKET; }
  "{"                         { return RosettaTokenTypes.LBRACE; }
  "}"                         { return RosettaTokenTypes.RBRACE; }
  "<"                         { return RosettaTokenTypes.LT; }
  ">"                         { return RosettaTokenTypes.GT; }
  "<="                        { return RosettaTokenTypes.LE; }
  ">="                        { return RosettaTokenTypes.GE; }
  "=="                        { return RosettaTokenTypes.EQ; }
  "!="                        { return RosettaTokenTypes.NE; }
  "+"                         { return RosettaTokenTypes.PLUS; }
  "-"                         { return RosettaTokenTypes.MINUS; }
  "*"                         { return RosettaTokenTypes.MULT; }
  "/"                         { return RosettaTokenTypes.DIV; }
  "->"                        { return RosettaTokenTypes.ARROW; }
  ".."                        { return RosettaTokenTypes.DOTDOT; }

  // Literals and identifiers
  {INTEGER}                   { return RosettaTokenTypes.INTEGER_LITERAL; }
  {STRING}                    { return RosettaTokenTypes.STRING_LITERAL; }
  {PATTERN}                   { return RosettaTokenTypes.PATTERN_LITERAL; }
  {IDENTIFIER}                { return RosettaTokenTypes.IDENTIFIER; }
}

[^] { return BAD_CHARACTER; }
