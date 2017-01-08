package ast;


import entity.ConstantEntry;
import type.TypeRef;

public class FloatLiteralNode extends LiteralNode {
    protected double value;
    protected ConstantEntry entry;

    public FloatLiteralNode(TypeRef ref, double value){
        super(ref);
        this.value = value;
    }

    public Double value(){
        return value;
    }

    public long rawValue(){
        return java.lang.Float.floatToRawIntBits((float)value);
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public void setEntry(ConstantEntry entry) {
        this.entry = entry;
    }

    public ConstantEntry entry(){
        return entry;
    }
}
