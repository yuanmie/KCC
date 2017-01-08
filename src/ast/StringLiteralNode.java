package ast;

import entity.ConstantEntry;
import type.TypeRef;

public class StringLiteralNode extends LiteralNode{
    protected String value;
    protected ConstantEntry entry;

    public StringLiteralNode(TypeRef ref, String value){
        super(ref);
        this.value = value;
    }

    public String value(){
        return value;
    }

    public ConstantEntry entry(){
        return entry;
    }

    public void setEntry(ConstantEntry ent){
        entry = ent;
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
