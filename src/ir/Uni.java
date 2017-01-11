package ir;

import asm.Type;

public class Uni extends Expr{
    protected Op op;
    protected Expr expr;

    public Uni(Type type, Op op, Expr expr){
        super(type);
        this.op = op;
        this.expr = expr;
    }

    public Op op(){
        return op;
    }

    public Expr expr(){
        return expr;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
