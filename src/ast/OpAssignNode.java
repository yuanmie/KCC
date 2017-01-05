package ast;

public class OpAssignNode extends AbstractAssignNode{
    protected String operator;

    public OpAssignNode(ExprNode lhs, String op, ExprNode rhs){
        super(lhs, rhs);
        this.operator = op;
    }

    public String operator(){
        return operator;
    }
}
