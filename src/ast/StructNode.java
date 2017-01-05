package ast;

import type.CompositeType;
import type.StructType;
import type.Type;
import type.TypeRef;

import java.util.List;

public class StructNode extends CompositeTypeDefinition{
    public StructNode(TypeRef ref, String name, List<Slot>membs){
        super(ref, name, membs);
    }

    public String kind(){
        return "struct";
    }

    public boolean isStruct(){
        return true;
    }

    public Type definingType(){
        return new StructType(name(), members());
    }
}
