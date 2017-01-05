package ast;

public class LogicalAndNode extends BinaryOpNode{
    public LogicalAndNode(ExprNode left, ExprNode right){
        super(left, "&&", right);
    }
}
