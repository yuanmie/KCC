package type;

public class FloatType extends Type{
    protected long size;
    protected String name;
    private int internSize = 3;

    public FloatType(long size, String name){
        super();
        this.size = internSize;
        this.name = name;
    }

    public boolean isFloat(){
        return true;
    }

    public boolean isScalar(){
        return true;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean isSameType(Type other) {
        if(!other.isFloat()){
            return false;
        }else{
            return equals(other.getFloatType());
        }
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isFloat();
    }

    @Override
    public boolean isCastableTo(Type target) {
        return target.isFloat() || target.isInteger() || target.isInt();
    }

    public boolean isSigned(){
        return false;
    }
}
