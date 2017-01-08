package ast;

import type.Type;

import java.util.List;

public class InitNode extends ExprNode{
    List<ExprNode> inits;
    public List<ExprNode> getInits() {
        return inits;
    }

    public void setInits(List<ExprNode> inits) {
        this.inits = inits;
    }


    public InitNode() {
        super();
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

}
