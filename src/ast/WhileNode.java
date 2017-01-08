package ast;

public class WhileNode extends StmtNode{
    protected StmtNode body;
    protected ExprNode cond;

    public WhileNode(ExprNode cond, StmtNode body){
        super();
        this.cond = cond;
        this.body = body;
    }

    public ExprNode cond(){
        return cond;
    }

    public StmtNode body(){
        return body;
    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
