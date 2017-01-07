package entity;

import type.ParamTypeRefs;
import type.TypeRef;

import java.util.ArrayList;
import java.util.List;

public class Params extends ParamSlots<CBCParameter>{
    public Params(List<CBCParameter> paramDescs){
        super(paramDescs, false);
    }

    public List<CBCParameter> parameters(){
        return paramDescriptors;
    }

    public ParamTypeRefs parametersTypeRef(){
        List<TypeRef> typeRefs = new ArrayList<TypeRef>();
        for(CBCParameter param : paramDescriptors){
            typeRefs.add(param.typeNode.typeRef());
        }
        return new ParamTypeRefs(typeRefs, vararg);
    }

    public boolean equals(Object other){
        return (other instanceof Params)
                && equals((Params)other);
    }
}
