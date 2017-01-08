package ast;

public class ReturnNode extends StmtNode{
    protected ExprNode expr;

    public ReturnNode(ExprNode expr){
        super();
        this.expr = expr;
    }

    public ExprNode expr(){
        return this.expr;
    }

    public void setExpr(ExprNode expr){
        this.expr = expr;
    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
