package ast;

import type.Type;
import type.TypeRef;

public class SizeofExprNode extends ExprNode{
    protected ExprNode expr;
    protected TypeNode type;

    public SizeofExprNode(ExprNode expr, TypeRef type){
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

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public TypeNode typeNode(){
        return this.type;
    }
}
