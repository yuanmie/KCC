package ir;

import asm.Label;

public class LabelStmt extends Stmt{
    protected Label label;

    public LabelStmt(Label label){
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
