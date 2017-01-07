package parser;


import ast.*;
import entity.*;
import type.*;

import java.io.*;
import java.util.*;

import static parser.TOK.TK_RETURN;

public class Parser {
    public static String SOURCE_ENCODING = "UTF-8";
    Token token;
    Declarations decls;
    IntegerLiteralNode Constant0 = integerNode("0");
    private Set<String> knownTypedefs = new HashSet<String>();

    public Parser(Token t) {
        this.token = t;
    }

    /*
    *********************************************************************
    * expression parse
    *
    *
    *
    *
    *********************************************************************
     */

    /**
     * primary-expression:
     * ID
     * constant
     * string-literal
     * ( expression )
     */
    ExprNode ParsePrimaryExpression() {
        ExprNode expr = null;

        switch (TOK.TK_CURRENT) {
            case TOK.TK_ID:
                expr = new VariableNode(token.stringValue);
                next_token();
                return expr;
            case TOK.TK_CHAR:
                expr = new IntegerLiteralNode(
                        IntegerTypeRef.charRef(),
                        characterCode(token.stringValue));
                next_token();
                return expr;
            case TOK.TK_INTCONST:        // 12345678
            case TOK.TK_UINTCONST:        // 12345678U
            case TOK.TK_LONGCONST:        // 12345678L
            case TOK.TK_ULONGCONST:        // 12345678UL
            case TOK.TK_LLONGCONST:        // 12345678LL
            case TOK.TK_ULLONGCONST:

                expr = integerNode(token.stringValue);// 12345678ULL
                next_token();
                return expr;
            case TOK.TK_FLOATCONST:        // 123.456f  123.456F
            case TOK.TK_DOUBLECONST:    // 123.456
            case TOK.TK_LDOUBLECONST:
                expr = floatNode(token.stringValue);// 123.456L	123.456l

                //CREATE_AST_NODE(expr, Expression);
                /**
                 see type.h  , enum{...,ULONGLONG,ENUM,FLOAT,}
                 */
                if (TOK.TK_CURRENT >= TOK.TK_FLOATCONST)
                    TOK.TK_CURRENT++;

                /// nasty, requires that both from TOK.TK_INTCONST to TOK.TK_LDOUBLECONST
                /// and from INT to LDOUBLE are consecutive
//                expr->ty = T(INT + TOK.TK_CURRENT - TOK.TK_INTCONST);
//                expr->op = OP_CONST;
//                expr->val = TokenValue;
                TOK.TK_CURRENT = token.nextToken();
                return expr;

            case TOK.TK_STRING:            // "ABC"
            case TOK.TK_WIDESTRING:        // L"ABC"

                ExprNode e = new StringLiteralNode(
                        new PointerTypeRef(IntegerTypeRef.charRef()),
                        token.stringValue);
                TOK.TK_CURRENT = token.nextToken();
                return e;

            case TOK.TK_LPAREN:        // (expr)

                TOK.TK_CURRENT = token.nextToken();
                expr = ParseExpression();
                Expect(TOK.TK_RPAREN);
                return expr;

            default:
                System.out.println("Expect identifier, string, constant or (");
                return Constant0;
        }
    }

    private long characterCode(String image) {
        String s = stringValue(image);
            if (s.length() != 1) {
                throw new Error("must not happen: character length > 1");
            }
            return (long)s.charAt(0);
    }


    private String stringValue(String _image) {
        int pos = 0;
        int idx;
        StringBuffer buf = new StringBuffer();
        String image = _image.substring(1, _image.length() - 1);

        while ((idx = image.indexOf("\\", pos)) >= 0) {
            buf.append(image.substring(pos, idx));
            if (image.length() >= idx + 4
                    && Character.isDigit(image.charAt(idx+1))
                    && Character.isDigit(image.charAt(idx+2))
                    && Character.isDigit(image.charAt(idx+3))) {
                buf.append(unescapeOctal(image.substring(idx+1, idx+4)));
                pos = idx + 4;
            }
            else {
                buf.append(unescapeSeq(image.charAt(idx+1)));
                pos = idx + 2;
            }
        }
        if (pos < image.length()) {
            buf.append(image.substring(pos, image.length()));
        }
        return buf.toString();
    }

    private char unescapeOctal(String digits)  {
        int i = Integer.parseInt(digits, 8);
        if (i > 255) {
            throw new RuntimeException(
                    "octal character sequence too big: \\" + digits);
        }
        return (char)i;
    }

    private char unescapeSeq(char c)   {
         final char bell = 7;
         final char backspace = 8;
         final char escape = 27;
         final char vt = 11;
        switch (c) {
            case '0': return '\0';
            case '"': return '"';
            case '\'': return '\'';
            case 'a': return bell;
            case 'b': return backspace;
            case 'e': return escape;
            case 'f': return '\f';
            case 'n': return '\n';
            case 'r': return '\r';
            case 't': return '\t';
            case 'v': return vt;
            default:
                throw new RuntimeException("unknown escape sequence: \"\\" + c);
        }
    }
    /**
     * postfix-expression:
     * primary-expression
     * postfix-expression [ expression ]
     * postfix-expression ( [argument-expression-list] )
     * postfix-expression . identifier
     * postfix-expression -> identifier
     * postfix-expression ++
     * postfix-expression --
     */
    ExprNode ParsePostfixExpression() {
        ExprNode expr, p;

        expr = ParsePrimaryExpression();

        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_LBRACKET:    // postfix-expression [ expression ]

                    TOK.TK_CURRENT = token.nextToken();
                    expr = new ArefNode(expr, ParseExpression());
                    Expect(TOK.TK_RBRACKET);
                    break;
                case TOK.TK_LPAREN:        // postfix-expression ( [argument-expression-list] )
                    TOK.TK_CURRENT = token.nextToken();
                    List<ExprNode> args = ParseArgs();
                  /*  if (TOK.TK_CURRENT != TOK.TK_RPAREN)
                    {
                        AstNode *tail;

                        /// function call expression's second kid is actually
                        /// a list of expression instead of a single expression
                        p->kids[1] = ParseAssignmentExpression();
                        tail = &p->kids[1]->next;
                        while (TOK.TK_CURRENT == TOK.TK_COMMA)
                        {
                           TOK.TK_CURRENT = token.nextToken()
                            *tail = (AstNode)ParseAssignmentExpression();
                            tail = &(*tail)->next;
                        }
                    }*/
                    Expect(TOK.TK_RPAREN);
                    expr = new FuncallNode(expr, args);
                    break;

                case TOK.TK_DOT:        // postfix-expression . identifier
                    next_token();
                    String memb = name();
                    expr = new MemberNode(expr, memb);
                    break;
                case TOK.TK_POINTER:    // postfix-expression -> identifier
                    next_token();
                    memb = name();
                    expr = new PtrMemberNode(expr, memb);
                    break;

                case TOK.TK_INC: // postfix-expression ++
                    expr = new SuffixOpNode("++", expr);
                    TOK.TK_CURRENT = token.nextToken();
                    break;
                case TOK.TK_DEC:    // postfix-expression --
                    expr = new SuffixOpNode("++", expr);
                    TOK.TK_CURRENT = token.nextToken();
                    break;
                default:
                    return expr;
            }
        }
    }

    private List<ExprNode> ParseArgs() {
        List<ExprNode> args = new ArrayList<ExprNode>();
        ExprNode arg;

        while (TOK.TK_CURRENT != TOK.TK_RPAREN) {
            arg = ParseAssignmentExpression();
            args.add(arg);
            if (TOK.TK_CURRENT == TOK.TK_COMMA) {
                next_token();
            }
        }
        return args;
    }


    /**
     * unary-expression:
     * postfix-expression
     * unary-operator unary-expression
     * ( type-name ) unary-expression
     * sizeof unary-expression
     * sizeof ( type-name )
     * <p>
     * unary-operator:
     * ++ -- & * + - ! ~
     */
    // The grammar of unary-expression in UCC is a little different from C89.
    // There is no cast-expression in UCC.
    ExprNode ParseUnaryExpression() {
        ExprNode expr;
        ExprNode n;
        int t;

        switch (TOK.TK_CURRENT) {
            case TOK.TK_INC:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new PrefixOpNode("++", n);

            case TOK.TK_DEC:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new PrefixOpNode("++", n);
            case TOK.TK_BITAND:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new AddressNode(n);
            case TOK.TK_MUL:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new DereferenceNode(n);
            case TOK.TK_ADD:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new UnaryOpNode("+", n);
            case TOK.TK_SUB:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new UnaryOpNode("-", n);
            case TOK.TK_NOT:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new UnaryOpNode("!", n);
            case TOK.TK_COMP:
                TOK.TK_CURRENT = token.nextToken();
                n = ParseUnaryExpression();
                return new UnaryOpNode("~", n);

            case TOK.TK_LPAREN:

                /// When current token is (, it may be a type cast expression
                /// or a primary expression, we need to look ahead one token,
                /// if next token is type name, the expression is treated as
                /// a type cast expression; otherwise a primary expresion
                token.BeginPeekToken();
                t = token.nextToken();
                if (IsTypeName(t)) {
                    token.EndPeekToken();
                    TOK.TK_CURRENT = token.nextToken();
                    TypeNode type = type();
                    type = postfixType(type);
                    Expect(TOK.TK_RPAREN);
                    expr = ParseUnaryExpression();

                    return new CastNode(type, expr);
                } else {
                    token.EndPeekToken();
                    return ParsePostfixExpression();
                }

            case TOK.TK_SIZEOF:

                /// this case hase the same issue with TOK.TK_LPAREN case

                TOK.TK_CURRENT = token.nextToken();
                if (TOK.TK_CURRENT == TOK.TK_LPAREN) {
                    token.BeginPeekToken();
                    t = token.nextToken();
                    // PRINT_DEBUG_INFO(("case TOK.TK_SIZEOF:"));
                    if (IsTypeName(t)) {
                        //  sizeof ( type-name )  -------------->  () is required here.
                        // sizeof(a)		sizeof  a
                        token.EndPeekToken();

                        TOK.TK_CURRENT = token.nextToken();
                        /// In this case, the first kid is not an expression,
                        /// but thanks to both type name and expression have a
                        /// kind member to discriminate them.
                        TypeNode type = type();
                        type = postfixType(type);
                        Expect(TOK.TK_RPAREN);
                        return new SizeofTypeNode(type, size_t());
                    } else {
                        // sizeof unary-expression
                        token.EndPeekToken();
                        expr = ParseUnaryExpression();
                        return new SizeofExprNode(expr, size_t());
                    }
                } else {
                    // sizeof unary-expression
                    expr = ParseUnaryExpression();
                    return new SizeofExprNode(expr, size_t());
                }

            default:
                return ParsePostfixExpression();
        }
    }

    /**
     * Parse a binary expression, from logical-OR-expresssion to multiplicative-expression
     */
    /**
     * @prec The precedence of Outer-Most operator of an binary expression
     * <p>
     * This function will parse the sub-expression with outer-most operator of @ prec precedence.
     * <p>
     * L4:		------>  ParseBinaryExpression(Prec[OP_OR]);
     * (The precedence of Outer-Most operator of L4  is Prec[OP_OR])
     * L5			||	L5	||	L5	||	...	|| L5
     * L5:		------>  ParseBinaryExpression(Prec[OP_AND]);
     * L6			&&	L6	&&	...			&& L6
     * L6:
     * L7			|	L7	|	...			|	L7
     * ...........
     * L13:	------> ParseBinaryExpression(Prec[OP_MUL]);
     * unary-expr			*	unary-expr	*	...		* unary-expr
     */

    ExprNode ParseBinaryExpression() {
        ExprNode l, r;
        l = expr8();
        while (true) {
            switch (TOK.TK_CURRENT) {

                case TOK.TK_OR:
                    next_token();
                    r = expr8();
                    l = new LogicalOrNode(l, r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr8() {
        ExprNode l, r;
        l = expr7();
        while (true) {
            switch (TOK.TK_CURRENT) {

                case TOK.TK_AND:
                    next_token();
                    r = expr7();
                    l = new LogicalAndNode(l, r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr7() {
        ExprNode l, r;
        l = expr6();
        while (true) {
            switch (TOK.TK_CURRENT) {

                case TOK.TK_GREAT:
                    next_token();
                    r = expr6();
                    l = new BinaryOpNode(l, ">", r);
                    break;
                case TOK.TK_LESS:
                    next_token();
                    r = expr6();
                    l = new BinaryOpNode(l, "<", r);
                    break;

                case TOK.TK_GREAT_EQ:
                    next_token();
                    r = expr6();
                    l = new BinaryOpNode(l, ">=", r);
                    break;
                case TOK.TK_LESS_EQ:
                    next_token();
                    r = expr6();
                    l = new BinaryOpNode(l, "<=", r);
                    break;
                case TOK.TK_EQUAL:
                    next_token();
                    r = expr6();
                    l = new BinaryOpNode(l, "==", r);
                    break;
                case TOK.TK_UNEQUAL:
                    next_token();
                    r = expr6();
                    l = new BinaryOpNode(l, "!=", r);
                    break;

                default:
                    return l;
            }
        }
    }

    ExprNode expr6() {
        ExprNode l, r;
        l = expr5();
        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_BITOR:
                    next_token();
                    r = expr5();
                    l = new BinaryOpNode(l, "|", r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr5() {
        ExprNode l, r;
        l = expr4();
        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_BITXOR:
                    next_token();
                    r = expr4();
                    l = new BinaryOpNode(l, "^", r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr4() {
        ExprNode l, r;
        l = expr3();
        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_BITAND:
                    next_token();
                    r = expr3();
                    l = new BinaryOpNode(l, "&", r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr3() {
        ExprNode l, r;
        l = expr2();
        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_LSHIFT:
                    next_token();
                    r = expr2();
                    l = new BinaryOpNode(l, "<<", r);
                    break;
                case TOK.TK_RSHIFT:
                    next_token();
                    r = expr2();
                    l = new BinaryOpNode(l, ">>", r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr2() {
        ExprNode l, r;
        l = expr1();
        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_ADD:
                    next_token();
                    r = expr1();
                    l = new BinaryOpNode(l, "+", r);
                    break;
                case TOK.TK_SUB:
                    next_token();
                    r = expr1();
                    l = new BinaryOpNode(l, "-", r);
                    break;
                default:
                    return l;
            }
        }
    }

    ExprNode expr1() {
        ExprNode l, r;
        l = ParseUnaryExpression();
        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_MUL:
                    next_token();
                    r = ParseUnaryExpression();
                    l = new BinaryOpNode(l, "*", r);
                    break;
                case TOK.TK_DIV:
                    next_token();
                    r = ParseUnaryExpression();
                    l = new BinaryOpNode(l, "/", r);
                    break;
                case TOK.TK_MOD:
                    next_token();
                    r = ParseUnaryExpression();
                    l = new BinaryOpNode(l, "%", r);
                    break;
                default:
                    return l;
            }
        }
    }

    private boolean IsBinaryOP(int tok) {
        return (tok >= TOK.TK_OR && tok <= TOK.TK_MOD);
    }


    /**
     * conditional-expression:
     * logical-OR-expression
     * logical-OR-expression ? expression : conditional-expression
     */
    ExprNode ParseConditionalExpression() {
        ExprNode c, t, e;

        c = ParseBinaryExpression();
        if (TOK.TK_CURRENT == TOK.TK_QUESTION) {
            ExprNode condExpr;


            TOK.TK_CURRENT = token.nextToken();

            t = ParseExpression();
            Expect(TOK.TK_COLON);
            e = ParseConditionalExpression();
            return new CondExprNode(c, t, e);
        }

        return c;
    }

    ExprNode initializer(){
        ExprNode expr = null;
        InitNode init  = null;
        if(TOK.TK_CURRENT == TOK.TK_LBRACE){
            init = new InitNode();
            next_token();
            init.setInits(initializer_list());
            if(TOK.TK_CURRENT == TOK.TK_COMMA){
                next_token();
            }
            Expect(TOK.TK_RBRACE);
            return init;
        }else{
            expr = ParseAssignmentExpression();
        }
        return expr;
    }

    private List<ExprNode> initializer_list() {
        List<ExprNode> inits = new LinkedList<ExprNode>();
        inits.add(initializer());
        while(TOK.TK_CURRENT != TOK.TK_RBRACE){
            if(TOK.TK_CURRENT == TOK.TK_COMMA){
                next_token();
                inits.add(initializer());
            }
        }
        return inits;
    }

    /**
     * assignment-expression:
     * conditional-expression
     * unary-expression assignment-operator assignment-expression
     * assignment-operator:
     * = *= /= %= += -= <<= >>= &= ^= |=
     * There is a little twist here: the parser always treats the first nonterminal
     * as a conditional expression.
     */
    ExprNode ParseAssignmentExpression() {
        ExprNode lhs, rhs, expr;
        /**
         It is not accurate here.
         We will check it during semantics .
         */
        lhs = ParseConditionalExpression();
        /**
         see token.h
         TOKEN(TK_ASSIGN,        "=")
         TOKEN(TK_BITOR_ASSIGN,  "|=")
         TOKEN(TK_BITXOR_ASSIGN, "^=")
         TOKEN(TK_BITAND_ASSIGN, "&=")
         TOKEN(TK_LSHIFT_ASSIGN, "<<=")
         TOKEN(TK_RSHIFT_ASSIGN, ">>=")
         TOKEN(TK_ADD_ASSIGN,    "+=")
         TOKEN(TK_SUB_ASSIGN,    "-=")
         TOKEN(TK_MUL_ASSIGN,    "*=")
         TOKEN(TK_DIV_ASSIGN,    "/=")
         TOKEN(TK_MOD_ASSIGN,    "%=")
         */
        if (TOK.TK_CURRENT >= TOK.TK_ASSIGN && TOK.TK_CURRENT <= TOK.TK_MOD_ASSIGN) {

            switch (TOK.TK_CURRENT) {
                case TOK.TK_ASSIGN:
                    TOK.TK_CURRENT = token.nextToken();
                    rhs = ParseAssignmentExpression();
                    return new AssignNode(lhs, rhs);
                default:
                    String op = opassign_op();
                    TOK.TK_CURRENT = token.nextToken();
                    rhs = ParseAssignmentExpression();
                    return new OpAssignNode(lhs, op, rhs);

            }
        }

        return lhs;
    }

    private String opassign_op() {
        switch (TOK.TK_CURRENT) {
            case TOK.TK_ADD_ASSIGN:
                return "+";
            case TOK.TK_SUB_ASSIGN:
                return "-";
            case TOK.TK_MUL_ASSIGN:
                return "*";
            case TOK.TK_DIV_ASSIGN:
                return "/";
            case TOK.TK_MOD_ASSIGN:
                return "%";
            case TOK.TK_BITAND_ASSIGN:
                return "&";
            case TOK.TK_BITOR_ASSIGN:
                return "|";
            case TOK.TK_BITXOR_ASSIGN:
                return "^";
            case TOK.TK_LSHIFT_ASSIGN:
                return "<<";
            case TOK.TK_RSHIFT_ASSIGN:
                return ">>";
            default:
                throw new RuntimeException("error opassign op!!!");
        }
    }

    /**
     * expression:
     * assignment-expression
     * expression , assignment-expression
     */
    ExprNode ParseExpression() {
        ExprNode expr, comma, assign;

        expr = ParseAssignmentExpression();
        while (TOK.TK_CURRENT == TOK.TK_COMMA) {

            TOK.TK_CURRENT = token.nextToken();
            assign = ParseAssignmentExpression();
            comma = new CommaNode(expr, assign);
            return comma;
        }
        return expr;
    }

    /**
     * Parse constant expression which is actually a conditional expression
     */
//	constant-expression:	conditional-expression
    ExprNode ParseConstantExpression() {
        return ParseConditionalExpression();
    }

    private TypeRef size_t() {
        return IntegerTypeRef.ulongRef();
    }

    private boolean IsTypeName(int t) {
        return t == TOK.TK_ID ? isType(token.stringValue) : (t >= TOK.TK_AUTO && t <= TOK.TK_VOID);
    }

    //暂时这样处理
    private boolean IsTypedefName(String stringValue) {
        return false;
    }

    private IntegerLiteralNode integerNode(String image) {
        long i = integerValue(image);
        if (image.endsWith("UL")) {
            return new IntegerLiteralNode(IntegerTypeRef.ulongRef(), i);
        } else if (image.endsWith("L")) {
            return new IntegerLiteralNode(IntegerTypeRef.longRef(), i);
        } else if (image.endsWith("U")) {
            return new IntegerLiteralNode(IntegerTypeRef.uintRef(), i);
        } else {
            return new IntegerLiteralNode(IntegerTypeRef.intRef(), i);
        }
    }

    private long integerValue(String image) {
        String s = image.replaceFirst("[UL]+", "");
        if (s.startsWith("0x") || s.startsWith("0X")) {
            return Long.parseLong(s.substring(2), 16);
        } else if (s.startsWith("0") && !s.equals("0")) {
            return Long.parseLong(s.substring(1), 8);
        } else {
            return Long.parseLong(s, 10);
        }
    }

    private FloatLiteralNode floatNode(String image) {
        Double i = floatValue(image);
        if (image.endsWith("UL")) {
            return new FloatLiteralNode(FloatTypeRef.floatRef(), i);
        } else if (image.endsWith("L")) {
            return new FloatLiteralNode(FloatTypeRef.floatRef(), i);
        } else if (image.endsWith("U")) {
            return new FloatLiteralNode(FloatTypeRef.floatRef(), i);
        } else {
            return new FloatLiteralNode(FloatTypeRef.floatRef(), i);
        }
    }

    private Double floatValue(String image) {
        String s = image.replaceFirst("[UL]+", "");
        return Double.parseDouble(s);
    }

    private String name() {
        String name = token.stringValue;
        next_token();
        return name;
    }

    /*
    *********************************************************************
    * Stmt parse
    *
    *
    *
    *
    *********************************************************************
     */
    StmtNode ParseStatement() {
        switch (TOK.TK_CURRENT) {
            case TOK.TK_ID:
                /**
                 In ParseLabelStatement(), ucl peek the next token
                 to determine whether it is label-statement or expression-statement.
                 (1)
                 loopAgain :		---------->   Starting with ID
                 ...
                 goto loopAgain

                 or
                 (2)
                 f(20,30)		---------->	Starting with ID


                 */
                token.BeginPeekToken();
                next_token();
                if(TOK.TK_CURRENT == TOK.TK_COLON){
                    token.EndPeekToken();
                    token.nextToken();
                    return ParseLabelStatement();
                }

                token.EndPeekToken();
                return ParseExpressionStatement();
            case TOK.TK_CASE:
                return ParseCaseStatement();

            case TOK.TK_DEFAULT:
                return ParseDefaultStatement();

            case TOK.TK_IF:
                return ParseIfStatement();

            case TOK.TK_SWITCH:
                return ParseSwitchStatement();

            case TOK.TK_WHILE:
                return ParseWhileStatement();

            case TOK.TK_DO:
                return ParseDoStatement();

            case TOK.TK_FOR:
                return ParseForStatement();

            case TOK.TK_GOTO:
                return ParseGotoStatement();

            case TOK.TK_CONTINUE:
                return ParseContinueStatement();

            case TOK.TK_BREAK:
                return ParseBreakStatement();

            case TK_RETURN:
                return ParseReturnStatement();

            case TOK.TK_LBRACE:
                return ParseCompoundStatement();

            default:
                return ParseExpressionStatement();
        }
    }

    private StmtNode ParseExpressionStatement() {
        ExprNode expr = null;
        if (TOK.TK_CURRENT != TOK.TK_SEMICOLON) {
            expr = ParseExpression();
        }
        Expect(TOK.TK_SEMICOLON);
        if(expr == null) return null;
        return new ExprStmtNode(expr);
    }

    private StmtNode ParseCompoundStatement() {
        List<DefinedVariable> vars;
        List<StmtNode> stmts = new ArrayList<StmtNode>();
        Expect(TOK.TK_LBRACE);
        vars = defvar_list();
        StmtNode s;
        while (true) {
            if (TOK.TK_CURRENT == TOK.TK_RBRACE) break;
            s = ParseStatement();
            if (s != null) {
                stmts.add(s);
            } else {
                break;
            }
        }
        Expect(TOK.TK_RBRACE);
        return new BlockNode(vars, stmts);
    }

    private ReturnNode ParseReturnStatement() {
        ExprNode expr;
        Expect(TK_RETURN);
        if (TOK.TK_CURRENT == TOK.TK_SEMICOLON) {
            next_token();
            return new ReturnNode(null);
        } else {
            expr = ParseExpression();
            Expect(TOK.TK_SEMICOLON);
            return new ReturnNode(expr);
        }
    }

    private StmtNode ParseBreakStatement() {
        Expect(TOK.TK_BREAK);
        Expect(TOK.TK_SEMICOLON);
        return new BreakNode();
    }

    private StmtNode ParseContinueStatement() {
        Expect(TOK.TK_CONTINUE);
        Expect(TOK.TK_SEMICOLON);
        return new ContinueNode();
    }

    private StmtNode ParseGotoStatement() {
        Expect(TOK.TK_GOTO);
        String name = name();
        Expect(TOK.TK_LPAREN);

        return new GotoNode(name);
    }

    private StmtNode ParseForStatement() {
        ExprNode init = null, cond = null, incr = null;
        StmtNode body;

        Expect(TOK.TK_FOR);
        Expect(TOK.TK_LPAREN);
        if (TOK.TK_CURRENT != TOK.TK_SEMICOLON) {
            init = ParseExpression();
        }
        Expect(TOK.TK_SEMICOLON);
        if (TOK.TK_CURRENT != TOK.TK_SEMICOLON) {
            cond = ParseExpression();
        }
        Expect(TOK.TK_SEMICOLON);
        if (TOK.TK_CURRENT != TOK.TK_SEMICOLON) {
            incr = ParseExpression();
        }
        Expect(TOK.TK_RPAREN);
        body = ParseStatement();
        return new ForNode(init, cond, incr, body);
    }

    private StmtNode ParseDoStatement() {
        ExprNode cond;
        StmtNode body;

        Expect(TOK.TK_DO);
        body = ParseStatement();
        Expect(TOK.TK_WHILE);
        Expect(TOK.TK_LPAREN);
        cond = ParseExpression();
        Expect(TOK.TK_RPAREN);
        Expect(TOK.TK_SEMICOLON);
        return new DoWhileNode(body, cond);
    }

    private StmtNode ParseWhileStatement() {
        ExprNode cond;
        StmtNode body;

        Expect(TOK.TK_WHILE);
        Expect(TOK.TK_LPAREN);
        cond = ParseExpression();
        Expect(TOK.TK_RPAREN);
        body = ParseStatement();
        return new WhileNode(cond, body);
    }

    private StmtNode ParseIfStatement() {
        ExprNode cond;
        StmtNode thenBody, elseBody = null;

        Expect(TOK.TK_IF);
        Expect(TOK.TK_LPAREN);
        cond = ParseExpression();
        Expect(TOK.TK_RPAREN);
        thenBody = ParseStatement();

        if (TOK.TK_CURRENT == TOK.TK_ELSE) {
            next_token();
            elseBody = ParseStatement();
        }

        return new IfNode(cond, thenBody, elseBody);

    }

    private SwitchNode ParseSwitchStatement() {
        ExprNode cond;
        List<CaseNode> bodies;

        next_token();
        Expect(TOK.TK_LPAREN);
        cond = ParseExpression();
        Expect(TOK.TK_RPAREN);
        Expect(TOK.TK_LBRACE);
        bodies = case_clauses();
        Expect(TOK.TK_RBRACE);
        return new SwitchNode(cond, bodies);
    }

    private List<CaseNode> case_clauses() {
        List<CaseNode> clauses = new ArrayList<CaseNode>();
        CaseNode n;

        while (true) {
            switch (TOK.TK_CURRENT) {
                case TOK.TK_CASE:
                    next_token();
                    n = ParseCaseStatement();
                    clauses.add(n);
                    break;
                case TOK.TK_DEFAULT:
                    n = ParseDefaultStatement();
                    clauses.add(n);
                    break;
                default:
                    return clauses;

            }
        }
    }

    private CaseNode ParseDefaultStatement() {
        BlockNode body;
        Expect(TOK.TK_DEFAULT);
        Expect(TOK.TK_COLON);
        body = case_body();
        return new CaseNode(new ArrayList<ExprNode>(), body);
    }

    private CaseNode ParseCaseStatement() {
        CaseNode caseStmt;
        List<ExprNode> values = cases();
        BlockNode body = case_body();
        return new CaseNode(values, body);
    }

    private BlockNode case_body() {
        LinkedList<StmtNode> stmts = new LinkedList<StmtNode>();
        StmtNode s;

        s = ParseStatement();
        if (s != null) {
            stmts.add(s);
        }
        while (TOK.TK_CURRENT != TOK.TK_CASE && TOK.TK_CURRENT != TOK.TK_DEFAULT && TOK.TK_CURRENT != TOK.TK_RBRACE ) {
            //if (TOK.TK_CURRENT == TOK.TK_BREAK) break;
            s = ParseStatement();
            if (s != null) {
                stmts.add(s);
            } else {
                break;
            }
        }
        if (!(stmts.getLast() instanceof BreakNode)) {
            throw new RuntimeException("miss break statement at the last of case clause");
        }
        return new BlockNode(new ArrayList<DefinedVariable>(), stmts);
    }

    private List<ExprNode> cases() {
        List<ExprNode> values = new ArrayList<ExprNode>();
        ExprNode n;
        n = ParsePrimaryExpression();
        values.add(n);
        Expect(TOK.TK_COLON);
        while (true) {
            if (TOK.TK_CURRENT == TOK.TK_CASE) {
                next_token();
                n = ParsePrimaryExpression();
                values.add(n);
                Expect(TOK.TK_COLON);
            } else {
                return values;
            }
        }
    }

    private LabelNode ParseLabelStatement() {
        String t = token.stringValue;
        StmtNode n;
        token.BeginPeekToken();
        if (TOK.TK_CURRENT == TOK.TK_COLON) {
            token.EndPeekToken();
            next_token();
            next_token();
            n = ParseStatement();
            return new LabelNode(t, n);
        } else {
            token.EndPeekToken();
            //return ParseExpressionStatement();
            return null;
        }
    }
 /*
    *********************************************************************
    * decl parse
    *
    *
    *
    *
    *********************************************************************
     */

    AST compilation_unit() {
        Declarations implidecl = new Declarations();
        next_token();
        if(TOK.TK_CURRENT == TOK.TK_EXTERN){
            implidecl = parseDecl();
        }

        decls = new Declarations();
        decls = top_defs();
        decls.add(implidecl);
        return new AST(decls);
    }

    private Declarations top_defs() {
        Declarations decls = new Declarations();
        DefinedFunction defun;
        List<DefinedVariable> defvars;
        Constant defconst;
        StructNode defstruct;
        UnionNode defunion;
        TypedefNode typedef;

        while(true) {
            token.BeginPeekToken();
            if (TOK.TK_CURRENT == TOK.TK_TYPEDEF) {
                token.EndPeekToken();
                next_token();
                typedef = typedef();
                decls.addTypedef(typedef);
            } else if (TOK.TK_CURRENT == TOK.TK_CONST) {
                token.EndPeekToken();
                next_token();
                defconst = defconst();
                decls.addConstant(defconst);
            } else if (TOK.TK_CURRENT == TOK.TK_ENUM) {
                token.EndPeekToken();
                next_token();
                //
                LinkedHashSet cs = defEnum();
                decls.addConstant(cs);
            } else {
                storage();
                typeref();
                name();
                if (TOK.TK_CURRENT == TOK.TK_LPAREN) {
                    token.EndPeekToken();
                    defun = defun();
                    decls.addDefun(defun);
                } else {
                    token.EndPeekToken();

                    token.BeginPeekToken();
                    storage();
                    type();
                    if (TOK.TK_CURRENT == TOK.TK_LBRACE) {
                        token.EndPeekToken();
                        if (TOK.TK_CURRENT == TOK.TK_STRUCT) {
                            defstruct = defstruct();
                            decls.addDefstruct(defstruct);
                        } else if (TOK.TK_CURRENT == TOK.TK_UNION) {
                            defunion = defunion();
                            decls.addDefunion(defunion);
                        } else {
                            //
                        }
                    } else {
                        if (TOK.TK_CURRENT == TOK.TK_ID) {
                            token.EndPeekToken();

                            defvars = defvars();
                            decls.addDefvars(defvars);
                        }else{
                            break;
                        }
                    }
                }
            }
        }
        return decls;
    }

    private LinkedHashSet<Constant> defEnum() {
        //name opt
        LinkedHashSet<Constant> constants = new LinkedHashSet<Constant>();
        String name = null;
        String lastSlot = "";
        String slot = null;
        TypeNode type = new TypeNode(IntegerTypeRef.intRef());
        ExprNode value;
        Constant constant = null;
        if(TOK.TK_CURRENT != TOK.TK_LBRACE){
            name = name();
        }
        Expect(TOK.TK_LBRACE);
        //deal const
        while(true){
            slot = name();
            if(TOK.TK_CURRENT == TOK.TK_ASSIGN){
                next_token();
                value = ParseAssignmentExpression();
                constant = new Constant(type, slot, value);
            }else{
                //deal first item
                if(constant == null){
                    constant = new Constant(type, slot, Constant0);
                }else{
                    ExprNode l = new VariableNode(lastSlot);
                    ExprNode r = integerNode("1");
                    value = new BinaryOpNode(l, "+", r);
                    constant = new Constant(type, slot, value);
                }

            }
            constants.add(constant);
            if(TOK.TK_CURRENT == TOK.TK_RBRACE) break;
            Expect(TOK.TK_COMMA);
            lastSlot = slot;
        }
        Expect(TOK.TK_RBRACE);
        Expect(TOK.TK_SEMICOLON);
        return constants;
    }

    private TypeNode type() {
        TypeRef ref;
        ref = typeref();
        if(ref == null){
            return null;
        }
        return new TypeNode(ref);
    }

    private boolean storage() {
        if (TOK.TK_CURRENT == TOK.TK_STATIC || TOK.TK_CURRENT == TOK.TK_EXTERN || TOK.TK_CURRENT == TOK.TK_REGISTER
                || TOK.TK_CURRENT == TOK.TK_AUTO) {

            if (TOK.TK_CURRENT == TOK.TK_STATIC) {
                boolean result = true;
                next_token();
                storage();
                return result;
            }
            next_token();
            return storage();
        } else if (TOK.TK_CURRENT == TOK.TK_CONST || TOK.TK_CURRENT == TOK.TK_VOLATILE) {
            next_token();
            return storage();
        }
        return false;
    }

    private UnionNode defunion() {
        Token t;
        String n;
        List<Slot> membs;

        Expect(TOK.TK_UNION);
        n = name();
        membs = member_list();
        Expect(TOK.TK_SEMICOLON);
        return new UnionNode(new UnionTypeRef(n), n, membs);
    }

    private StructNode defstruct() {
        Token t;
        String n;
        List<Slot> membs;

        Expect(TOK.TK_STRUCT);
        n = name();
        membs = member_list();
        Expect(TOK.TK_SEMICOLON);
        return new StructNode(new StructTypeRef(n), n, membs);
    }

    private List<Slot> member_list() {
        List<Slot> membs = new ArrayList<Slot>();
        Slot s;

        Expect(TOK.TK_LBRACE);
        while (TOK.TK_CURRENT != TOK.TK_RBRACE) {
            s = slot();
            Expect(TOK.TK_SEMICOLON);
            membs.add(s);
        }
        Expect(TOK.TK_RBRACE);
        return membs;
    }

    private Slot slot() {
        TypeNode t;
        String n;
        t = type();
        n = name();
        t = postfixType(t);
        ExprNode c;
        if (TOK.TK_CURRENT == TOK.TK_COLON) {
            next_token();
            c = ParseConstantExpression();
            return new Slot(t, n, c);
        }
        return new Slot(t, n);
    }

    private List<DefinedVariable> defvars() {
        List<DefinedVariable> defs = new ArrayList<DefinedVariable>();
        boolean priv;
        TypeNode type;
        String name;
        ExprNode init = null;


        priv = storage();
        type = type();
        name = name();
        type = postfixType(type);

        if (TOK.TK_CURRENT == TOK.TK_ASSIGN) {
            next_token();
            init = initializer();
            defs.add(new DefinedVariable(priv, type, name, init));
            init = null;
        }else{
            defs.add(new DefinedVariable(priv, type, name, init));
        }

        while (TOK.TK_CURRENT == TOK.TK_COMMA) {
            next_token();
            name = name();
            if(TOK.TK_CURRENT == TOK.TK_ASSIGN){
                Expect(TOK.TK_ASSIGN);
                init = ParseAssignmentExpression();
            }
            defs.add(new DefinedVariable(priv, type, name, init));
            init = null;
        }
        Expect(TOK.TK_SEMICOLON);
        return defs;
    }

    private DefinedFunction defun() {
        boolean priv;
        TypeRef ret;
        String n;
        Params ps;
        BlockNode body;
        priv = storage();
        ret = typeref();
        n = name();
        postfixTyperef(ret);
        Expect(TOK.TK_LPAREN);
        ps = params();
        body = block();
        TypeRef t = new FunctionTypeRef(ret, ps.parametersTypeRef());
        return new DefinedFunction(priv, new TypeNode(t), n, ps, body);
    }

    private BlockNode block() {

        List<DefinedVariable> vars;
        List<StmtNode> stmts;
        Expect(TOK.TK_LBRACE);
        vars = defvar_list();
        stmts = stmts();
        Expect(TOK.TK_RBRACE);
        return new BlockNode(vars, stmts);
    }

    private List<StmtNode> stmts() {
        List<StmtNode> ss = new ArrayList<StmtNode>();
        StmtNode s;

        while (TOK.TK_CURRENT != TOK.TK_RBRACE) {
            s = ParseStatement();
            if (s != null) ss.add(s);
        }
        return ss;
    }


    private List<DefinedVariable> defvar_list() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        List<DefinedVariable> vars;

        while (encouterVar()) {
            vars = defvars();
            result.addAll(vars);
        }
        ;
        return result;
    }

    private boolean encouterVar() {
        boolean result = false;
        //patch
        int peektok = TOK.TK_CURRENT;
        String peekStringValue = token.stringValue;
        int peekindex = token.index;

        token.BeginPeekToken();
        storage();
        if(TOK.TK_CURRENT == TOK.TK_LPAREN){
            token.EndPeekToken();
            return result;
        }
        TypeNode type = type();
        if (type == null) {
            result = false;
        } else if (TOK.TK_CURRENT != TOK.TK_ID) {
            result = false;
        } else {
            result = true;
        }
        token.EndPeekToken();
        TOK.TK_CURRENT = peektok;
        token.stringValue = peekStringValue;
        token.index = peekindex;
        return result;
    }

    private Params params() {
        String t;
        Params params;
        token.BeginPeekToken();
        if(TOK.TK_CURRENT == TOK.TK_RPAREN){
            token.EndPeekToken();
            next_token();
            return new Params(new ArrayList<CBCParameter>());
        }
        if (TOK.TK_CURRENT == TOK.TK_VOID) {
            next_token();
            if (TOK.TK_CURRENT == TOK.TK_RPAREN) {
                next_token();
                return new Params(new ArrayList<CBCParameter>());
            } else {
                token.EndPeekToken();
            }
        }
        params = fixedparams();
        if (TOK.TK_CURRENT == TOK.TK_ELLIPSIS) {
            next_token();
            params.acceptVarargs();
        }
        Expect(TOK.TK_RPAREN);
        return params;
    }

    private Params fixedparams() {
        List<CBCParameter> params = new ArrayList<CBCParameter>();
        CBCParameter param, param1;

        param = param();
        params.add(param);
        while (TOK.TK_CURRENT == TOK.TK_COMMA) {
            next_token();
            if (TOK.TK_CURRENT == TOK.TK_ELLIPSIS) {
                return new Params(params);
            } else {
                param = param();
                params.add(param);
            }
        }
        return new Params(params);
    }

    private CBCParameter param() {

        TypeNode t;
        String n;
        t = type();
        n = name();
        t = postfixType(t);
        return new CBCParameter(t, n);
    }

    private Constant defconst() {
        TypeNode type;
        String name;
        ExprNode value;

        type = type();
        name = name();
        Expect(TOK.TK_ASSIGN);
        value = ParseExpression();
        Expect(TOK.TK_SEMICOLON);
        return new Constant(type, name, value);
    }

    private TypedefNode typedef() {
        TypeRef ref;
        ref = typeref();
        String name = name();
        ref = postfixTyperef(ref);
        Expect(TOK.TK_SEMICOLON);
        addType(name);
        return new TypedefNode(ref, name);

    }

    private TypeRef postfixTyperef(TypeRef ref) {
        //parse postfix declare, ex: array
        while (TOK.TK_CURRENT == TOK.TK_LBRACKET) {
            next_token();
            if (TOK.TK_CURRENT == TOK.TK_RBRACKET) {
                ref = new ArrayTypeRef(ref);
            } else {
                next_token();
                ref = new ArrayTypeRef(ref, integerValue(token.stringValue));
            }
            next_token();
        }
        return ref;
    }

    private TypeNode postfixType(TypeNode type) {
        if(type == null) return null;
        TypeRef ref = postfixTyperef(type.typeRef);
        //parse postfix declare, ex: array
        return new TypeNode(ref);
    }

    private TypeRef typeref() {
        TypeRef ref;
        Token t;
        ParamTypeRefs params;
        ref = typeref_base();
        if(ref == null) return null;
        while (true) {
            if (TOK.TK_CURRENT == TOK.TK_MUL) {
                next_token();
                ref = new PointerTypeRef(ref);
            } else if (TOK.TK_CURRENT == TOK.TK_LPAREN) {
                next_token();
                params = param_typerefs();
                ref = new FunctionTypeRef(ref, params);
            } else {
                break;
            }
        }
        return ref;
    }

    private ParamTypeRefs param_typerefs() {
        ParamTypeRefs params;
        token.BeginPeekToken();
        if (TOK.TK_CURRENT == TOK.TK_VOID) {
            next_token();
            if (TOK.TK_CURRENT == TOK.TK_RPAREN) {
                next_token();
                return new ParamTypeRefs(new ArrayList<TypeRef>());
            } else {
                token.EndPeekToken();
            }
        }
        params = fixedparam_typerefs();
        if (TOK.TK_CURRENT == TOK.TK_ELLIPSIS) {
            next_token();
            params.acceptVarargs();
        }
        Expect(TOK.TK_RPAREN);
        return params;
    }

    private ParamTypeRefs fixedparam_typerefs() {
        List<TypeRef> refs = new ArrayList<TypeRef>();
        TypeRef ref;
        ref = typeref();
        refs.add(ref);
        while (TOK.TK_CURRENT == TOK.TK_COMMA) {
            next_token();
            if (TOK.TK_CURRENT == TOK.TK_ELLIPSIS) {
                return new ParamTypeRefs(refs);
            } else {
                ref = typeref();
                refs.add(ref);
            }
        }
        return new ParamTypeRefs(refs);
    }

    private TypeRef typeref_base() {
        String t, name;

        switch (TOK.TK_CURRENT) {
            case TOK.TK_VOID:
                next_token();
                return new VoidTypeRef();
            case TOK.TK_CHAR:
                next_token();
                return IntegerTypeRef.charRef();
            case TOK.TK_SHORT:
                next_token();
                if (TOK.TK_CURRENT == TOK.TK_INT) {
                    next_token();
                    //
                    return IntegerTypeRef.shortRef();
                }
                return IntegerTypeRef.shortRef();
            case TOK.TK_INT:
                next_token();
                return IntegerTypeRef.intRef();
            case TOK.TK_LONG:
                next_token();
                if (TOK.TK_CURRENT == TOK.TK_INT) {
                    next_token();
                    return IntegerTypeRef.longRef();
                } else if (TOK.TK_CURRENT == TOK.TK_DOUBLE) {
                    next_token();
                    return FloatTypeRef.DoubleRef();
                }
                return IntegerTypeRef.longRef();
            case TOK.TK_FLOAT:
                next_token();
                return FloatTypeRef.floatRef();
            case TOK.TK_DOUBLE:
                next_token();
                return FloatTypeRef.DoubleRef();
            case TOK.TK_SIGNED:
                next_token();
                if (TOK.TK_CURRENT == TOK.TK_CHAR) {
                    next_token();
                    return IntegerTypeRef.charRef();
                } else if (TOK.TK_CURRENT == TOK.TK_SHORT) {
                    next_token();
                    if (TOK.TK_CURRENT == TOK.TK_INT) {
                        next_token();
                        return IntegerTypeRef.shortRef();
                    }
                    return IntegerTypeRef.shortRef();
                } else if (TOK.TK_CURRENT == TOK.TK_INT) {
                    next_token();
                    return IntegerTypeRef.intRef();
                } else if (TOK.TK_CURRENT == TOK.TK_LONG) {
                    next_token();
                    if (TOK.TK_CURRENT == TOK.TK_INT) {
                        next_token();
                    }
                    return IntegerTypeRef.longRef();
                }
                return IntegerTypeRef.intRef();
            case TOK.TK_UNSIGNED:
                next_token();
                if (TOK.TK_CURRENT == TOK.TK_CHAR) {
                    next_token();
                    return IntegerTypeRef.ucharRef();
                } else if (TOK.TK_CURRENT == TOK.TK_SHORT) {
                    next_token();
                    if (TOK.TK_CURRENT == TOK.TK_INT) {
                        next_token();
                    }
                    return IntegerTypeRef.ushortRef();
                } else if (TOK.TK_CURRENT == TOK.TK_INT) {
                    next_token();
                    return IntegerTypeRef.uintRef();
                } else if (TOK.TK_CURRENT == TOK.TK_LONG) {
                    next_token();
                    return IntegerTypeRef.ulongRef();
                }
                return IntegerTypeRef.uintRef();
            case TOK.TK_STRUCT:
                next_token();
                name = name();
                return new StructTypeRef(name);
            case TOK.TK_UNION:
                next_token();
                name = name();
                return new UnionTypeRef(name);
            case TOK.TK_ENUM:
                next_token();
                name();
                return IntegerTypeRef.intRef();
            default:
                if (isType(token.stringValue)) {
                     name = token.stringValue;
                    next_token();
                    return new UserTypeRef(name);
                }
                return null;
        }
    }

    /*    Declarations declaration_file()
        {
            Declarations impdecls, decls = new Declarations();
            UndefinedFunction funcdecl;
            UndefinedVariable vardecl;
            Constant defconst;
            StructNode defstruct;
            UnionNode defunion;
            TypedefNode typedef;

            ( LOOKAHEAD(<EXTERN> typeref() <IDENTIFIER> "(")
            funcdecl=funcdecl()   { decls.addFuncdecl(funcdecl); }
            | vardecl=vardecl()     { decls.addVardecl(vardecl); }
            | defconst=defconst()   { decls.addConstant(defconst); }
            | defstruct=defstruct() { decls.addDefstruct(defstruct); }
            | defunion=defunion()   { decls.addDefunion(defunion); }
            | typedef=typedef()     { decls.addTypedef(typedef); }
            )*
            <EOF>
                {
            return decls;
            }
        }*/
    Declarations parseDecl(){
        Declarations  decls = new Declarations();
        while(TOK.TK_CURRENT == TOK.TK_EXTERN){
            Expect(TOK.TK_EXTERN);
            {
                TypeRef ret;
                String n;
                Params ps;
                ret = typeref();
                n = name();
                ret = postfixTyperef(ret);
                if(TOK.TK_CURRENT == TOK.TK_SEMICOLON){
                    Expect(TOK.TK_SEMICOLON);
                    decls.addVardecl(new UndefinedVariable(new TypeNode(ret), n));
                }else{
                    Expect(TOK.TK_LPAREN);
                    ps = params();
                    Expect(TOK.TK_SEMICOLON);
                    TypeRef t = new FunctionTypeRef(ret, ps.parametersTypeRef());
                    decls.addFuncdecl(new UndefinedFunction(new TypeNode(t), n, ps));
                }
            }
        }
        return decls;
    }


    private boolean isType(String name) {
        return knownTypedefs.contains(name);
    }

    private void addType(String name) {
        knownTypedefs.add(name);
    }


    public void next_token() {
        TOK.TK_CURRENT = token.nextToken();
    }

    private void Expect(int tok) {
        if (TOK.TK_CURRENT == tok) {
            TOK.TK_CURRENT = token.nextToken();
        }else{
            throw new RuntimeException("expect " + tok);
        }
    }

    public static AST parseFile(File file) {

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = fileReader.readLine()) != null) {
                sb.append(line+"\n");
            }
            Token t = new Token(sb.toString());
            Parser parser = new Parser(t);

            return parser.compilation_unit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

  /*  public Parser(Token token){
        this.token = token;
    }
    FunctionNode parse(){
        return parseFunction();
    }

    FunctionNode parseFunction(){
        storage();
        typeRef();
        String t = name();
        //token.nextToken();
        expect("(");
        parseParam();
        expect(")");
        BlockNode b = parse_block();
        return new FunctionNode(t,b);
    }

    private BlockNode parse_block() {
        expect("{");
        ReturnNode r = stmts();
        expect("}");
        return new BlockNode(r);
    }

    private void defvar_list() {
        storage();
        type();
        name();
    }

    private ReturnNode stmts() {
        return return_stms();
    }

    private ReturnNode return_stms() {
        expect("return");
        if(peek(";")){
            token.nextToken();
            return new ReturnNode(null);
        }else{
            ExprNode expr = expr();
            expect(";");
            return new ReturnNode(expr);

        }
    }

    private ExprNode expr() {

        String t = name();
        return new ExprNode(new IntegerLiteralNode(Integer.parseInt(t)));
    }



    private void parseParam() {
        if(token.peekToken().equals("void")){
            token.nextToken();
            if(token.peekToken().equals(")")){
                token.nextToken();
                return ;
            }else{
                name();
                if(peek(")")) return;
                else expect(",");
            }
        }

        fixedParams();
    }

    private void fixedParams() {
       param();
       while(!peek(")")){
           expect(",");
           param();
       }
    }

    private void param() {
        type();
        name();
    }

    private void type() {
        typeRef();
    }

    private void typeRef() {
        typeref_base();
        while(peek("*")) token.nextToken();
    }

    private void typeref_base() {
       if(Try("int") || Try("char"));
    }

    private void expect(String t) {
        if(!token.peekToken().equals(t)){
            throw new RuntimeException("expect " + t);
        }else{
            token.nextToken();
        }
    }

    private boolean peek(String t){
        return t.equals(token.peekToken());
    }

    private void storage() {
        Try("static");
    }

    private boolean Try(String t) {
        if(token.peekToken().equals(t)) {
            token.nextToken();
            return true;
        }
        return false;
    }*/
}
