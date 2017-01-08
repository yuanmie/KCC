package ast;

import type.Type;

public class CommaNode extends AbstractAssignNode {
    public CommaNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public Type type() {
        return rhs.type();
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
