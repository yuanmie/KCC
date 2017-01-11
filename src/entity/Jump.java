package entity;

import asm.Label;
import ir.IRVisitor;
import ir.Stmt;

public class Jump extends Stmt {
    protected Label label;

    public Jump(Label label){
        this.label = label;
    }

    public Label label(){
        return label;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
