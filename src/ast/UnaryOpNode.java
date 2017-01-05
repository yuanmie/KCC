package ast;

import type.Type;

public class UnaryOpNode extends ExprNode{
    protected String operator;
    protected ExprNode expr;
    protected Type opType;

    public UnaryOpNode(String op, ExprNode expr){
        this.operator = op;
        this.expr = expr;
    }

    public String operator(){
        return operator;
    }

    public Type type(){
        return expr.type();
    }

    public void setOpType(Type t){
        this.opType = t;
    }

    public Type opType(){
        return opType;
    }

    public ExprNode expr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }
}
