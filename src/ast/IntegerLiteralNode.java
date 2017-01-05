package ast;

import type.Type;
import type.TypeRef;

public class IntegerLiteralNode extends LiteralNode{
    protected long value;

    public IntegerLiteralNode(TypeRef ref, long value){
        super(ref);
        this.value = value;
    }

    public IntegerLiteralNode(TypeRef ref,
                              Type type, long value){
        super(ref);
        this.value = value;
        this.typeNode.setType(type);
    }

    public long value(){
        return value;
    }
}
