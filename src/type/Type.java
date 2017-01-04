package type;

abstract public class Type {
    static final public long sizeUnknown = -1;

    abstract public long size();
    public long allocSize(){
        return size();
    }

    public long alignment(){
        return allocSize();
    }

    abstract public boolean isSameType(Type other);

    public boolean isVoid(){
        return false;
    }

    public boolean isInt(){
        return false;
    }

    public boolean isInteger(){
        return false;
    }

    public boolean isFloat(){
        return false;
    }

    public boolean isSigned(){
        throw new Error("#isSigned for non-integer type");
    }

    public boolean isPointer(){
        return false;
    }

    public boolean isArray(){
        return false;
    }

    public boolean isCompositeType(){
        return false;
    }

    public boolean isStruct(){
        return false;
    }

    public boolean isUnion(){
        return false;
    }

    public boolean isUserType(){
        return false;
    }

    public boolean isFunction(){
        return false;
    }

    public boolean isAllocatedArray(){
        return false;
    }

    public boolean isIncompleteArray(){
        return false;
    }

    public boolean isScalar(){
        return false;
    }

    public boolean isCallable(){
        return false;
    }

    abstract public boolean isCompatible(Type other);
    abstract public boolean isCastableTo(Type target);

    public Type baseType(){
        throw new Error("baseType called for underferable type");
    }

    public IntegerType getIntegerType() {
        return (IntegerType)this;
    }

    public FloatType getFloatType() {
        return (FloatType)this;
    }

    public FunctionType getFunctionType() {
        return (FunctionType)this;
    }

    //dosomething

}
