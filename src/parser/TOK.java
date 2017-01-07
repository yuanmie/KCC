package parser;

public class TOK {
    final static int  i = 0;
    final static int  TK_BEGIN= 0;
    final static int  TK_AUTO= 1;
    final static int  TK_EXTERN= 2;
    final static int  TK_REGISTER= 3;
    final static int  TK_STATIC= 4;
    final static int  TK_TYPEDEF= 5;
    final static int  TK_CONST= 6;
    final static int  TK_VOLATILE= 7;
    final static int  TK_SIGNED= 8;
    final static int  TK_UNSIGNED= 9;
    final static int  TK_SHORT= 10;
    final static int  TK_LONG= 11;
    final static int  TK_CHAR= 12;
    final static int  TK_INT= 13;
    final static int  TK_INT64= 14;
    final static int  TK_FLOAT= 15;
    final static int  TK_DOUBLE= 16;
    final static int  TK_ENUM= 17;
    final static int  TK_STRUCT= 18;
    final static int  TK_UNION= 19;
    final static int  TK_VOID= 20;
    final static int  TK_BREAK= 21;
    final static int  TK_CASE= 22;
    final static int  TK_CONTINUE= 23;
    final static int  TK_DEFAULT= 24;
    final static int  TK_DO= 25;
    final static int  TK_ELSE= 26;
    final static int  TK_FOR= 27;
    final static int  TK_GOTO= 28;
    final static int  TK_IF= 29;
    final static int  TK_RETURN= 30;
    final static int  TK_SWITCH= 31;
    final static int  TK_WHILE= 32;
    final static int  TK_SIZEOF= 33;

//identifier
    final static int  TK_ID= 34;

//constant
    final static int  TK_INTCONST= 35;
    final static int  TK_UINTCONST= 36;
    final static int  TK_LONGCONST= 37;
    final static int  TK_ULONGCONST= 38;
    final static int  TK_LLONGCONST= 39;
    final static int  TK_ULLONGCONST= 40;
    final static int  TK_FLOATCONST= 41;
    final static int  TK_DOUBLECONST= 42;
    final static int  TK_LDOUBLECONST= 43;
    final static int  TK_STRING= 44;
    final static int  TK_WIDESTRING= 45;

//operators
    final static int  TK_COMMA= 46;
    final static int  TK_QUESTION= 47;
    final static int  TK_COLON= 48;
    final static int  TK_ASSIGN= 49;
    final static int  TK_BITOR_ASSIGN= 50;
    final static int  TK_BITXOR_ASSIGN= 51;
    final static int  TK_BITAND_ASSIGN= 52;
    final static int  TK_LSHIFT_ASSIGN= 53;
    final static int  TK_RSHIFT_ASSIGN= 54;
    final static int  TK_ADD_ASSIGN= 55;
    final static int  TK_SUB_ASSIGN= 56;
    final static int  TK_MUL_ASSIGN= 57;
    final static int  TK_DIV_ASSIGN= 58;
    final static int  TK_MOD_ASSIGN= 59;
    final static int  TK_OR= 60;
    final static int  TK_AND= 61;
    final static int  TK_BITOR= 62;
    final static int  TK_BITXOR= 63;
    final static int  TK_BITAND= 64;
    final static int  TK_EQUAL= 65;
    final static int  TK_UNEQUAL= 66;
    final static int  TK_GREAT= 67;
    final static int  TK_LESS= 68;
    final static int  TK_GREAT_EQ= 69;
    final static int  TK_LESS_EQ= 70;
    final static int  TK_LSHIFT= 71;
    final static int  TK_RSHIFT= 72;
    final static int  TK_ADD= 73;
    final static int  TK_SUB= 74;
    final static int  TK_MUL= 75;
    final static int  TK_DIV= 76;
    final static int  TK_MOD= 77;
    final static int  TK_INC= 78;
    final static int  TK_DEC= 79;
    final static int  TK_NOT= 80;
    final static int  TK_COMP= 81;
    final static int  TK_DOT= 82;
    final static int  TK_POINTER= 83;
    final static int  TK_LPAREN= 84;
    final static int  TK_RPAREN= 85;
    final static int  TK_LBRACKET= 86;
    final static int  TK_RBRACKET= 87;

//punctuators
    final static int  TK_LBRACE= 88;
    final static int  TK_RBRACE= 89;
    final static int  TK_SEMICOLON= 90;
    final static int  TK_ELLIPSIS= 91;
    final static int  TK_POUND= 92;
    final static int  TK_NEWLINE= 93;

    final static int  TK_END= 94;
    final static int  TK_ERROR= 95;
    static int  TK_CURRENT; //current tok
}
