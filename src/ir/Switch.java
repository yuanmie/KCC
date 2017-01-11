package ir;

import asm.Label;

import java.util.List;

public class Switch extends Stmt{
    protected Expr cond;
    protected List<Case> cases;
    protected Label defaultLabel, endLable;

    public Switch(Expr cond, List<Case> cases, Label defaultLabel, Label endLable){
        this.cond = cond;
        this.cases = cases;
        this.defaultLabel = defaultLabel;
        this.endLable = endLable;
    }

    public Expr cond(){
        return cond;
    }

    public List<Case> cases(){
        return cases;
    }

    public Label defaultLabel(){
        return defaultLabel;
    }

    public Label endLabel(){
        return endLable;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
