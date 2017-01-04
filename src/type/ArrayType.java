package type;

public class ArrayType extends Type{
    protected Type baseType;
    protected long length;
    protected long pointerSize;
    static final protected long undefined = -1;

    public ArrayType(Type baseType, long pointerSize){
        this(baseType, undefined, pointerSize);
    }

    public ArrayType(Type baseType, long length, long pointerSize){
        this.baseType = baseType;
        this.length = length;
        this.pointerSize = pointerSize;
    }

    public boolean isArray(){
        return true;
    }

    public boolean isAllocatedArray(){
        return length != undefined && (!baseType.isArray()
                || baseType.isAllocatedArray());
    }

    public boolean isIncompleteArray(){
        if(!baseType.isArray()){
            return false;
        }else{
            return !baseType.isAllocatedArray();
        }
    }

    public Type baseType() {
        return baseType;
    }

    public long length() {
        return length;
    }

    public long size() {
        return pointerSize;
    }

    public long allocSize(){
        if(length == undefined){
            return size();
        }else{
            return baseType.allocSize() * length;
        }
    }

    public long alignment(){
        return baseType.alignment();
    }

    public boolean equals(Object other){
        if(!(other instanceof ArrayType)){
            return false;
        }else{
            ArrayType type = (ArrayType)other;
            return (baseType.equals(type.baseType) && length == type.length);
        }
    }
    @Override
    public boolean isSameType(Type other) {
        if(!other.isPointer() && !other.isArray()){
            return false;
        }else{
            return baseType.isSameType(other.baseType());
        }
    }

    @Override
    public boolean isCompatible(Type target) {
        if(!target.isPointer() && !target.isArray()){
            return false;
        }else{
            if(target.baseType().isVoid()){
                return true;
            }else{
                return baseType.isCompatible(target.baseType()) &&
                        baseType.size() == target.baseType().size();
            }
        }
    }

    @Override
    public boolean isCastableTo(Type target) {
        return target.isPointer() || target.isArray();
    }
}
