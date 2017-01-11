package ir;

public class Assign extends Stmt {
    protected Expr lhs, rhs;


    public Assign(Expr lhs, Expr rhs){
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Expr lhs(){
        return lhs;
    }

    public Expr rhs(){
        return rhs;
    }

    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
