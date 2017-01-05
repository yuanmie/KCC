package ast;

public class SuffixOpNode extends UnaryArithmeticOpNode{
    public SuffixOpNode(String op, ExprNode expr){
        super(op, expr);
    }
}
