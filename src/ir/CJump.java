package ir;

import asm.Label;

public class CJump extends Stmt{
    protected Expr cond;
    protected Label thenLabel;
    protected Label elseLabel;

    public CJump(Expr cond, Label thenLabel, Label elseLabel) {
        this.cond = cond;
        this.thenLabel = thenLabel;
        this.elseLabel = elseLabel;
    }

    public Expr cond(){
        return cond;
    }

    public Label thenLabel(){
        return thenLabel;
    }

    public Label elseLabel(){
        return elseLabel;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
