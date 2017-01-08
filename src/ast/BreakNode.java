package ast;

public class BreakNode extends StmtNode{
    public BreakNode(){
        super();
    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
