package type;

import static sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte1.other;

public class PointerType extends Type{
    protected long size;
    protected Type baseType;

    public PointerType(long size, Type baseType) {
        this.size = size;
        this.baseType = baseType;
    }

    public boolean isPointer(){
        return true;
    }

    public boolean isScalar(){
        return true;
    }

    public boolean isCallable(){
        return baseType.isFunction();
    }

    public Type baseType(){
        return baseType;
    }

    public boolean equals(Object other){
        if(!(other instanceof PointerType)){
            return false;
        }else{
            return baseType.isSameType(((PointerType) other).baseType);
        }
    }
    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean isSameType(Type other) {
        if(!other.isPointer()){
            return false;
        }else{
            return baseType.isSameType(other.baseType());
        }
    }

    @Override
    public boolean isCompatible(Type other) {
        if(!other.isPointer()){
            return false;
        }

        if(baseType.isVoid()){
            return true;
        }
        return baseType.isCompatible(other.baseType());
    }

    @Override
    public boolean isCastableTo(Type target) {
        return target.isPointer() || target.isInteger();
    }
}
