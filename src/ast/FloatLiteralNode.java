package ast;


import type.TypeRef;

public class FloatLiteralNode extends LiteralNode {
    protected double value;
    //dosomething

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
}
