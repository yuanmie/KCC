package ir;

import asm.ImmediateValue;
import asm.IntegerLiteral;
import asm.MemoryReference;
import asm.Type;

public class Int extends Expr{
    protected long value;

    public Int(Type type, long value) {
        super(type);
        this.value = value;
    }

    public long value(){
        return value;
    }

    public boolean isConstant(){
        return true;
    }

    public ImmediateValue asmValue(){
        return new ImmediateValue(new IntegerLiteral(value));
    }

    public MemoryReference memref(){
        throw new Error("must not happen: Int value memref");
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
