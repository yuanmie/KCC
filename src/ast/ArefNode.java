package ast;

import type.ArrayType;
import type.Type;

public class ArefNode extends LHSNode{
    private ExprNode expr,  index;

    public ArefNode(ExprNode expr, ExprNode index) {
        this.expr = expr;
        this.index = index;
    }

    public ExprNode expr(){
        return expr;
    }

    public ExprNode index(){
        return index;
    }

    public boolean isMultiDimension(){
        return (expr instanceof ArefNode) &&
              !expr.origType().isPointer();
    }

    public ExprNode baseExpr(){
        return isMultiDimension() ? ((ArefNode)expr).baseExpr() : expr;
    }

    public long elementSize(){
        return origType().allocSize();
    }

    public long length(){
        return ((ArrayType)expr.origType()).length();
    }


    @Override
    protected Type origType() {
        return expr.origType().baseType();
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
