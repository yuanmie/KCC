package ast;

import type.Type;
import type.TypeRef;
import type.UserType;
import type.UserTypeRef;

public class TypeDefNode extends TypeDefinition {
    protected TypeNode real;

    public TypeDefNode(TypeRef real, String name){
        super(new UserTypeRef(name), name);
        this.real = new TypeNode(real);
    }

    public boolean isUserType(){
        return true;
    }

    public TypeNode realTypeNode(){
        return real;
    }

    public Type realType(){
        return real.type();
    }

    public TypeRef realTypeRef(){
        return real.typeRef();
    }

    public Type definingType(){
        return new UserType(name(), realTypeNode());
    }
}
