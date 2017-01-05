package ast;

import type.Type;
import type.TypeRef;

public class SizeOfExprNode extends ExprNode{
    protected ExprNode expr;
    protected TypeNode type;

    public SizeOfExprNode(ExprNode expr, TypeRef type){
        this.expr = expr;
        this.type = new TypeNode(type);
    }

    public ExprNode expr(){
        return this.expr;
    }

    public void setExpr(ExprNode expr){
        this.expr = expr;
    }

    public Type type(){
        return this.type.type();
    }

    public TypeNode typeNode(){
        return this.type;
    }
}
