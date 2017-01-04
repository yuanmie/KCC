package ast;

import type.Type;
import type.TypeRef;

public class TypeNode extends Node{
    public TypeRef typeRef;
    Type type;

    public TypeNode(TypeRef ref){
        super();
        this.typeRef = ref;
    }

    public TypeNode(Type type){
        super();
        this.type = type;
    }
    public TypeRef typeRef(){
        return typeRef;
    }

    public boolean isResolved(){
        return type != null;
    }

    public void setType(Type t){
        if(type != null){
            throw new Error("TypeNode setType called twice");
        }else{
            type = t;
        }
    }

    public Type type(){
        if(type == null){
            throw new Error("TypeNode not resolved: " + typeRef);
        }else{
            return type;
        }
    }
}
