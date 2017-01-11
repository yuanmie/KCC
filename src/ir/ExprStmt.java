package ir;

public class ExprStmt extends Stmt{
    protected Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }

    public Expr expr(){
        return expr;
    }

    public <S, E> S accept(IRVisitor<S, E> visitor){
        return visitor.visit(this);
    }
}
