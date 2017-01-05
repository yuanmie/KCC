package ast;

import type.Type;
import type.TypeRef;

public class LiteralNode extends ExprNode{
    protected TypeNode typeNode;

    public LiteralNode(TypeRef ref){
        super();
        this.typeNode = new TypeNode(ref);
    }

    public Type type(){
        return typeNode.type();
    }

    public TypeNode typeNode(){
        return typeNode;
    }

    public boolean isConstant(){
        return true;
    }
}
