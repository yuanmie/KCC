package ast;

public class ContinueNode extends StmtNode{
    public ContinueNode(){

    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

}
