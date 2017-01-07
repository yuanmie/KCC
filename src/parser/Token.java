package parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Token {
    public String text;
    public int length;
    public int index;
    public char[] charArray;

    //value
    int intValue;
    float fValue;
    double dValue;
    String stringValue;

    String peekStringValue;
    Object object;


    private int peekIndex;
    private int peekTOK;


    public Token() {
    }

    ;

    public Token(String text) {
        this.text = text;
        this.charArray = text.toCharArray();
        this.index = 0;
        this.length = text.length();
    }

    public int nextToken() {
        skipSpace();
        if (eof()) return TOK.TK_END;
        char ch = charArray[index];
        switch (ch) {
            case '\'':
                return ScanCharLiteral();
            case '"':
                return ScanStringLiteral();
            case '+':
                return ScanPlus();
            case '-':
                return ScanMinus();
            case '*':
                return ScanStar();
            case '/':
                return ScanSlash();
            case '%':
                return ScanPercent();
            case '<':
                return ScanLess();
            case '>':
                return ScanGreat();
            case '!':
                return ScanExclamation();
            case '=':
                return ScanEqual();
            case '|':
                return ScanBar();
            case '&':
                return ScanAmpersand();
            case '^':
                return ScanCaret();
            case '.':
                return ScanDot();
            case '{':
                ++index;
                return TOK.TK_LBRACE;
            case '}':
                ++index;
                return TOK.TK_RBRACE;
            case '[':
                ++index;
                return TOK.TK_LBRACKET;
            case ']':
                ++index;
                return TOK.TK_RBRACKET;
            case '(':
                ++index;
                return TOK.TK_LPAREN;
            case ')':
                ++index;
                return TOK.TK_RPAREN;
            case ',':
                ++index;
                return TOK.TK_COMMA;
            case ';':
                ++index;
                return TOK.TK_SEMICOLON;
            case '~':
                ++index;
                return TOK.TK_COMP;
            case '?':
                ++index;
                return TOK.TK_QUESTION;
            case ':':
                ++index;
                return TOK.TK_COLON;
            default:
                if (ch == '_' || isAlpha(ch)) {
                    return ScanIdentifier();
                } else if (isDigit(ch)) {
                    return ScanNumericLiteral();
                } else {
                    return TOK.TK_ERROR;
                }
        }
    }

    private String nextNumber() {
        String sign = "";
        int oldIndex = index;
        char ch = charArray[index];
        index++;
        if (ch == '0') {

        } else {
            while (!eof() && isDigit(charArray[index])) {
                index++;
            }
        }
        if (!eof() && charArray[index] == '.') {
            ++index;
            while (!eof() && isDigit(charArray[index])) {
                index++;
            }

            if (!eof() && (charArray[index] == 'e' || charArray[index] == 'E')) {
                ++index;
                if (!eof() && (charArray[index] == '+' || charArray[index] == '-')) {
                    ++index;
                }
                while (!eof() && isDigit(charArray[index])) {
                    index++;
                }
            }
        }

        return sign + text.substring(oldIndex, index);
    }

    private void skipSpace() {
        while (!eof() && (charArray[index] == ' ' || charArray[index] == '\n' ||
                charArray[index] == '\t' ||
                charArray[index] == '\f' || charArray[index] == '#')) {
            char ch = charArray[index];
            //does not parse temporarily
//            switch(ch){
//                case '\n':
//                case '#':
//                case '/':
//            }
            ++index;
        }
    }

    //encouter \
    char escapeChar(int wide) {
        char vt = 11;
        char escape = 27;
        index++;
        int v = 0;
        boolean overflow = false;
        switch (charArray[index++]) {
            // does't support
            // case 'a':
            // return 'a';
            case 'a': return 7;
            case 'b':
                return '\b';

            case 'f':
                return '\f';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 't':
                return '\t';
            case 'v':
                return  vt;
            case 'e':
                return escape;
            case '"':
                return '"';
            case 'x':        //		\xhh	hexical
                if (!IsHexDigit(charArray[index])) {
                    System.out.println("Expect hex digit");
                    return 'x';
                }

                while (IsHexDigit(charArray[index])) {
                    /**
                     Bug?
                     if(v >> (WCharType->size * 8-4 ))
                     (1) WCharType->size == 2
                     0xABCD * 16 + value --> overflow
                     0x0ABC is OK.
                     (2) WCharType->size == 4
                     0x12345678  * 16 + value --> overflow
                     0x01234567 is OK.
                     */
                    if ((v >> (4 * 8 - 4)) > 0) {
                        overflow = true;
                    }
                    //  v= v * 16 + value,  value : 0-9  A-F
                    if (isDigit(charArray[index])) {
                        v = v * 16 + charArray[index] - '0';

                    } else {
                        v = v * 16 + ToUpper(charArray[index]) - 'A' + 10;
                    }
                    index++;
                }
                if (overflow || ((wide > 0) && v > 255)) {
                    System.out.println("Hexademical espace sequence overflow");
                }
                return (char)v;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':    // \ddd	octal
                v = charArray[index - 1] - '0';
                if (IsOctDigit(charArray[index])) {
                    v = (v << 3) + charArray[index++] - '0';
                    if (IsOctDigit(charArray[index]))
                        v = (v << 3) + charArray[index++] - '0';
                }
                return (char)v;

            default:
                System.out.printf("Unrecognized escape sequence:\\%c", charArray[index]);
                return charArray[index];
        }
    }

    int FindKeyword(String str) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("auto", TOK.TK_AUTO);
        map.put("break", TOK.TK_BREAK);
        map.put("case", TOK.TK_CASE);
        map.put("char", TOK.TK_CHAR);
        map.put("const", TOK.TK_CONST);
        map.put("continue", TOK.TK_CONTINUE);
        map.put("default", TOK.TK_DEFAULT);
        map.put("do", TOK.TK_DO);
        map.put("double", TOK.TK_DOUBLE);
        map.put("else", TOK.TK_ELSE);
        map.put("enum", TOK.TK_ENUM);
        map.put("extern", TOK.TK_EXTERN);
        map.put("float", TOK.TK_FLOAT);
        map.put("for", TOK.TK_FOR);
        map.put("goto", TOK.TK_GOTO);
        map.put("if", TOK.TK_IF);
        map.put("int", TOK.TK_INT);
        map.put("long", TOK.TK_LONG);
        map.put("register", TOK.TK_REGISTER);
        map.put("return", TOK.TK_RETURN);
        map.put("short", TOK.TK_SHORT);
        map.put("signed", TOK.TK_SIGNED);
        map.put("sizeof", TOK.TK_SIZEOF);
        map.put("switch", TOK.TK_SWITCH);
        map.put("struct", TOK.TK_STRUCT);
        map.put("static", TOK.TK_STATIC);
        map.put("typedef", TOK.TK_TYPEDEF);
        map.put("union", TOK.TK_UNION);
        map.put("unsigned", TOK.TK_UNSIGNED);
        map.put("void", TOK.TK_VOID);
        map.put("volatile", TOK.TK_VOLATILE);
        map.put("while", TOK.TK_WHILE);
        return map.get(str) == null ? TOK.TK_ID : map.get(str);
    }

    int ScanIntLiteral(int start, int len, int base) {
        int p = start;
        int end = start + len;
        boolean overflow = false;
        int[] i = {0, 0};
        int tok = TOK.TK_INTCONST;
        int d = 0;
        int carry0 = 0, carry1 = 0;

        intValue = 0;
        while (p != end) {
            if (base == 16) {
                if ((charArray[p] >= 'A' && charArray[p] <= 'F') ||
                        (charArray[p] >= 'a' && charArray[p] <= 'f')) {
                    d = ToUpper(charArray[p]) - 'A' + 10;
                } else {
                    d = charArray[p] - '0';
                }
            } else {
                d = charArray[p] - '0';
            }
            /**
             treat i[1],i[0] as 64 bit integer.
             */
            switch (base)
            {
                case 16:
                    intValue = intValue * 16 + d;
                    break;

                case 8:
                    intValue = intValue * 8 + d;
                    break;

                case 10:

                    intValue = intValue * 10 + d;
                    break;
            }
          /*  if (i[0] > UINT_MAX - d)	// for decimal, i[0] + d maybe greater than UINT_MAX
            {
                carry0 += i[0] - (UINT_MAX - d);
            }
            if (carry1 || (i[1] > UINT_MAX - carry0))
            {
                overflow = 1;
            }
            i[0] += d;
            i[1] += carry0;
            p++;*/
          p++;
        }
        /**
         overflow != 0:
         out of 64 bit bound
         i[1] != 0
         out of 32 bit bound
         */
        if (overflow || i[1] != 0) {
            System.out.println("Integer literal is too big");
        }

        tok = TOK.TK_INTCONST;

        /**
         12345678U
         12345678u
         */
        if (charArray[index] == 'U' || charArray[index] == 'u') {
            index++;
            if (tok == TOK.TK_INTCONST) {
                tok = TOK.TK_UINTCONST;
            } else if (tok == TOK.TK_LLONGCONST) {
                tok = TOK.TK_ULLONGCONST;
            }
        }
        /**
         12345678UL
         12345678L
         */
        if (charArray[index] == 'L' || charArray[index] == 'l') {
            index++;
            if (tok == TOK.TK_INTCONST) {
                tok = TOK.TK_LONGCONST;
            } else if (tok == TOK.TK_UINTCONST) {
                tok = TOK.TK_ULONGCONST;
            }
            if (charArray[index] == 'L' || charArray[index] == 'l')    // LL  long long int
            {
                index++;
                if (tok < TOK.TK_LLONGCONST) {
                    tok = TOK.TK_LLONGCONST;
                }
            }
        }

        return tok;
    }

    int ScanFloatLiteral(int start) {
        double d;
        /**
         Just check the optional fragment part and the
         exponent part.
         The value of float number is determined by
         strtod().
         */
        if (charArray[index] == '.') {
            index++;
            while (isDigit(charArray[index])) {
                index++;
            }
        }
        if (charArray[index] == 'e' || charArray[index] == 'E') {
            index++;
            if (charArray[index] == '+' || charArray[index] == '-') {
                index++;
            }
            if (!isDigit(charArray[index])) {
                System.err.println("Expect exponent value");
            } else {
                while (isDigit(charArray[index])) {
                    index++;
                }
            }
        }

        TOK.TK_CURRENT = TOK.TK_FLOATCONST;
        d = Double.parseDouble(text.substring(start, index));
        this.stringValue = text.substring(start, index);
        //TokenValue.d = d;
        // single precision float:  123.456f
        if (charArray[index] == 'f' || charArray[index] == 'F') {
            index++;
            fValue = (float)d;
            //TokenValue.f = (float)d;
            return TOK.TK_FLOATCONST;
        } else if (charArray[index] == 'L' || charArray[index] == 'l')    // long double
        {
            index++;
            dValue = d;
            return TOK.TK_LDOUBLECONST;
        } else {
            dValue = d;
            return TOK.TK_DOUBLECONST;
        }
    }

    int ScanNumericLiteral() {
        int start = index;
        int base = 10;

        if (charArray[index] == '.')    // float  .123
        {
            return ScanFloatLiteral(start);
        }

        if (charArray[index] == '0' && (charArray[index + 1] == 'x' || charArray[index + 1] == 'X'))    // hexical  0x123ABC / 0X123ABC
        {
            index += 2;
            start = index;
            base = 16;
            if (!IsHexDigit(charArray[index])) {
                System.err.println("Expect hex digit");
                //TokenValue.i[0] = 0;
                return TOK.TK_INTCONST;
            }
            while (IsHexDigit(charArray[index]))    // 123ABC of  0x123ABC
            {
                index++;
            }
        } else if (charArray[index] == '0')    // octal		01234567
        {
            index++;
            base = 8;
            while (IsOctDigit(charArray[index]))    // 1234567 of 0123567
            {
                index++;
            }
        } else    // decimal	123456789
        {
            index++;
            while (isDigit(charArray[index])) {
                index++;
            }
        }
        /**
         (1)  if number starts with 0x
         or
         (2) if  charArray[index] are not part of a float number
         we are sure that we encounter a int literal.  base = 8/10/16
         */
        if (base == 16 || (charArray[index] != '.' && charArray[index] != 'e' && charArray[index] != 'E')) {
            this.stringValue = text.substring(start, index);
            if(base == 16){
                this.stringValue = "0x" + this.stringValue;
            }
            return ScanIntLiteral(start, (int) (index - start), base);
        } else {
            // 123.456
            return ScanFloatLiteral(start);
        }
    }

    int ScanCharLiteral() {

        int ch = 0;
        int n = 0;
        int count = 0;
        int wide = 0;

        if (charArray[index] == 'L')    // wide char			L'a'		L'\t'
        {
            index++;
            wide = 1;
        }
        int oldindex = index++;            // skip \'
        if (charArray[index] == '\'') {
            //Error(&TokenCoord, "empty character constant");
        } else if (charArray[index] == '\n' || eof()) {
            //Error(&TokenCoord,"missing terminating ' character");
        } else {
            if (charArray[index] == '\\') {
                ch = (int) escapeChar(wide);
            } else {
                if (wide > 0) {
               /* n = mbrtowc(&ch, index, MB_CUR_MAX, 0);
                if(n > 0){
                    index += n;
                }*/
                    // PRINT_DEBUG_INFO(("%x %x",n,ch));
                } else {
                    ch = charArray[index];
                    index++;
                }
            }
            while (charArray[index] != '\'')        // L'abc',  skip the redundant characters
            {
                if (charArray[index] == '\n' || eof())
                    break;
                index++;
                count++;
            }
        }


        if (charArray[index] != '\'') {
            //Error(&TokenCoord, "missing terminating ' character");
        }

        index++;
        if (count > 0) {
            //Warning(&TokenCoord, "Two many characters");
        }
        intValue = ch;
        this.stringValue = this.text.substring(oldindex, index);
        return TOK.TK_CHAR;

    }

    int ScanStringLiteral()    // "abc"  or L"abc"
    {

        StringBuffer sb = new StringBuffer();
        StringBuffer str = new StringBuffer();
        int wide = 0;
        char ch;
        while (true) {
            if (charArray[index] == 'L') {
                index++;
                wide = 1;
                // char tmp[512] --> int tmp[512/sizeof(int)]
            }
            int oldIndex = index;
            index++;
            // skip "
            while (charArray[index] != '"') {
                if (eof() || charArray[index] == '\n')
                    break;
                if (charArray[index] == '\\') {

                    ch = escapeChar(wide);
                } else {
            /*if(wide){
                n = mbrtowc(&ch, index, MB_CUR_MAX, 0);
                if(n > 0){
                    index += n;
                }else{
                    ch = charArray[index];
                    index++;
                }
                // PRINT_DEBUG_INFO(("%x %x",n,ch));
            }else{*/
                    ch = charArray[index];
                    index++;
                }
                str.append(ch);
            }

            if (charArray[index] != '"') {
                //Error(&TokenCoord, "Expect \"");
                return 0;
            }
            index++;        // skip "
            sb.append(str);
            skipSpace();
            if (charArray[index] == '"')        // "abc"		"123"	---> "abc123"
            {
                index++;
            } else    // L"abc"	L"123"	--> L"abc123"
            {
                break;
            }

        }
        stringValue = sb.toString();
        return TOK.TK_STRING;
    }

    int ScanIdentifier() {
        int start = index;
        int tok = 0;

//        if (charArray[index] == 'L')	// special case :  wide char/string
//        {
//            if (index[1] == '\'')
//            {
//                return ScanCharLiteral();		// L'a'	wide char
//            }
//            if (index[1] == '"')
//            {
//                return ScanStringLiteral();	// 	L"wide string"
//            }
//        }
        // lettter(letter|digit)*
        index++;
        while (IsLetterOrDigit(charArray[index]) || charArray[index] == '_') {
            index++;
        }

        String id = text.substring(start, index);
        tok = FindKeyword(id);
        if (tok == TOK.TK_ID) {
            stringValue = id;
        }
        return tok;
    }

    int ScanPlus() {
        index++;
        if (charArray[index] == '+') {
            index++;
            return TOK.TK_INC;            // ++
        } else if (charArray[index] == '=') {
            index++;
            return TOK.TK_ADD_ASSIGN;    // +=
        } else {
            return TOK.TK_ADD;            // +
        }
    }

    int ScanMinus() {
        index++;
        if (charArray[index] == '-') {
            index++;
            return TOK.TK_DEC;            // --
        } else if (charArray[index] == '=') {
            index++;
            return TOK.TK_SUB_ASSIGN;    // -=
        } else if (charArray[index] == '>') {
            index++;
            return TOK.TK_POINTER;        // ->
        } else {
            return TOK.TK_SUB;            // -
        }
    }

    int ScanStar() {
        index++;
        if (charArray[index] == '=') {
            index++;
            return TOK.TK_MUL_ASSIGN;        // *=
        } else {
            return TOK.TK_MUL;                // *
        }
    }

    int ScanSlash() {
        index++;
        if (charArray[index] == '=') {
            index++;
            return TOK.TK_DIV_ASSIGN;        //	 /=
        } else {
            return TOK.TK_DIV;                // 		/
        }
    }

    int ScanPercent() {
        index++;
        if (charArray[index] == '=') {
            index++;
            return TOK.TK_MOD_ASSIGN;        // %=
        } else {
            return TOK.TK_MOD;                // %
        }
    }

    int ScanLess() {
        index++;
        if (charArray[index] == '<') {
            index++;
            if (charArray[index] == '=') {
                index++;
                return TOK.TK_LSHIFT_ASSIGN;        // <<=
            }
            return TOK.TK_LSHIFT;        // <<
        } else if (charArray[index] == '=') {
            index++;
            return TOK.TK_LESS_EQ;        // <=
        } else {
            return TOK.TK_LESS;            // <
        }
    }

    int ScanGreat() {
        index++;
        if (charArray[index] == '>') {
            index++;
            if (charArray[index] == '=')        // >>=
            {
                index++;
                return TOK.TK_RSHIFT_ASSIGN;
            }
            return TOK.TK_RSHIFT;        // >>
        } else if (charArray[index] == '=') {
            index++;
            return TOK.TK_GREAT_EQ;            // >=
        } else {
            return TOK.TK_GREAT;        // >
        }
    }

    int ScanExclamation() {
        index++;
        if (charArray[index] == '=')        // !=
        {
            index++;
            return TOK.TK_UNEQUAL;
        } else        // !
        {
            return TOK.TK_NOT;
        }
    }

    int ScanEqual() {
        index++;
        if (charArray[index] == '=')        // ==
        {
            index++;
            return TOK.TK_EQUAL;
        } else    // =
        {
            return TOK.TK_ASSIGN;
        }
    }

    int ScanBar() {
        index++;
        if (charArray[index] == '|')        // ||
        {
            index++;
            return TOK.TK_OR;
        } else if (charArray[index] == '=')    // |=
        {
            index++;
            return TOK.TK_BITOR_ASSIGN;
        } else    // |
        {
            return TOK.TK_BITOR;
        }
    }

    int ScanAmpersand() {
        index++;
        if (charArray[index] == '&')        // &&
        {
            index++;
            return TOK.TK_AND;
        } else if (charArray[index] == '=')    // &=
        {
            index++;
            return TOK.TK_BITAND_ASSIGN;
        } else        // &
        {
            return TOK.TK_BITAND;
        }
    }

    int ScanCaret()    // ^	exclusive or
    {
        index++;
        if (charArray[index] == '=') {
            index++;
            return TOK.TK_BITXOR_ASSIGN;
        } else {
            return TOK.TK_BITXOR;
        }
    }

    int ScanDot() {
        if (isDigit(charArray[index + 1]))    // .123		float number
        {
            return ScanFloatLiteral(index);
        } else if (charArray[index + 1] == '.' && charArray[index + 2] == '.')    //  ... 	variable parameters.
        {
            index += 3;
            return TOK.TK_ELLIPSIS;
        } else    // just a simple dot
        {
            index++;
            return TOK.TK_DOT;
        }
    }

    private boolean IsLetterOrDigit(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean IsOctDigit(char c) {
        return c >= '0' && c <= '8';
    }

    private int ToUpper(char c) {
        return Character.toUpperCase(c);
    }

    private boolean IsHexDigit(char c) {
        return (isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
    }

    private String nextIdentifier() {
        int sourceIndex = index++;
        while (!eof() && isAlpha(charArray[index]) || isDigit(charArray[index])) {
            ++index;
        }
        return text.substring(sourceIndex, index);
    }

    boolean eof() {
        boolean result = text == null || index >= length;
        if(result) TOK.TK_CURRENT = TOK.TK_END;
        return result;
    }

    boolean isAlpha(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    boolean isDigit(int c) {
        return (c >= '0' && c <= '9');
    }

    int peekToken() {
        int oldIndex = index;
        int result = nextToken();
        index = oldIndex;
        return result;
    }

    public static void main(String[] args) {
        File file = new File("/home/likai/java/kcc/src/com/yuanmie/Compiler/hello.c");
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = fileReader.readLine()) != null) {
                sb.append(line);
            }
            Token t = new Token(sb.toString());
            while (!t.eof()) {
                System.out.println(t.nextToken());
            }
//            Parser parser = new Parser(t);
//            FunctionNode f = parser.parse();
//            Assember assember = new Assember();
//            assember.emit(f);
            int i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void BeginPeekToken() {
        peekTOK = TOK.TK_CURRENT;
        peekIndex = index;
        peekStringValue = stringValue;
    }

    public void EndPeekToken() {
        stringValue = peekStringValue;
        index = peekIndex;
        TOK.TK_CURRENT = peekTOK;
    }
}
