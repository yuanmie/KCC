package ir;

public class Return extends Stmt{
    protected Expr expr;

    public Return(Expr expr){
        this.expr = expr;
    }

    public Expr expr(){
        return expr;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
