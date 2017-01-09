package type;

import java.util.List;

public class FunctionType extends Type{

    protected Type returnType;
    protected ParamTypes paramTypes;

    public FunctionType(Type returnType, ParamTypes paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    public boolean isFunction(){
        return true;
    }

    public boolean isCallable(){
        return true;
    }

    @Override
    public long size() {
        throw new Error("FunctionType size called");
    }

    @Override
    public boolean isSameType(Type other) {
        if(!other.isFunction()){
            return false;
        }else{
            FunctionType type = other.getFunctionType();
            return type.returnType.isSameType(returnType)
                    && type.paramTypes.isSameType(paramTypes);
        }
    }

    @Override
    public boolean isCompatible(Type target) {
        if(!target.isFunction()){
            return false;
        }else{
            FunctionType type = target.getFunctionType();
            return type.returnType.isCompatible(returnType)
                    && type.paramTypes.isSameType(paramTypes);
        }
    }

    @Override
    public boolean isCastableTo(Type target) {
        return target.isFunction();
    }

    public Type returnType(){
        return returnType;
    }

    public boolean isVararg(){
        return paramTypes.isVararg();
    }

    public boolean acceptsArgs(long numArgs){
        if(paramTypes.isVararg()){
            return (numArgs >= paramTypes.minArgc());
        }else{
            return (numArgs == paramTypes.argc());
        }
    }

    public List<Type> paramTypes(){
        return paramTypes.types();
    }

    public long alignment(){
        throw new Error("FunctionType alignment called");
    }
}
