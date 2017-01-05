package ast;

import type.TypeRef;

public class StringLiteralNode extends LiteralNode{
    protected String value;

    public StringLiteralNode(TypeRef ref, String value){
        super(ref);
        this.value = value;
    }

    public String value(){
        return value;
    }

}
