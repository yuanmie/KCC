package ast;

import type.Type;

public class CastNode extends ExprNode{
    protected TypeNode typeNode;
    protected ExprNode expr;

    public CastNode(Type t, ExprNode expr){
        this(new TypeNode(t), expr);
    }

    public CastNode(TypeNode t, ExprNode expr){
        this.typeNode = t;
        this.expr = expr;
    }

    public Type type() {
        return typeNode.type();
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public ExprNode expr() {
        return expr;
    }

    public boolean isLvalue(){
        return expr.isLvalue();
    }

    public boolean isAssignable(){
        return expr.isAssignable();
    }

    public boolean isEffectiveCast(){
        return (type().isFloat() && expr.type().size() == 4 ||
        type().size() > expr.type().size());
    }
}
