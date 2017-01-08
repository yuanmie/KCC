package ast;

import compiler.TypeResolver;
import type.Type;
import type.TypeRef;

abstract public class TypeDefinition extends Node{
    protected String name;
    protected TypeNode typeNode;

    public TypeDefinition(TypeRef ref, String name){
        this.name = name;
        this.typeNode = new TypeNode(ref);
    }

    public String name() {
        return name;
    }


    public TypeNode typeNode() {
        return typeNode;
    }

    public TypeRef typeRef() {
        return typeNode.typeRef();
    }

    public Type type() {
        return typeNode.type();
    }

    abstract public Type definingType();

    abstract public <T> T accept(DeclarationVisitor<T> visitor);
}
