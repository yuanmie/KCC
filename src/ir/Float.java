package ir;

import asm.ImmediateValue;
import asm.MemoryReference;
import asm.Type;
import entity.ConstantEntry;
import entity.ConstantTable;

public class Float extends Expr{
    protected double value;

    protected ConstantEntry entry;

    public Float(Type type, double value) {
        super(type);
        this.value = value;
    }

    public Float(Type type, ConstantEntry entry){
        super(type);
        this.entry = entry;

    }
    public double value(){
        return value;
    }

    public long rawValue(){
        return java.lang.Float.floatToRawIntBits((float)value);
    }

    public boolean isConstant(){
        return true;
    }

    public ImmediateValue asmValue(){
        return entry.address();
    }

    public MemoryReference memref(){
        throw new Error("must not happen: floatvlaue memref");
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
