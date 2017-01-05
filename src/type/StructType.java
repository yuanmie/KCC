package type;

import ast.Slot;
import utils.AsmUtils;

import java.util.List;

public class StructType extends CompositeType{
    public StructType(String name, List<Slot> members){
        super(name, members);
    }

    public boolean isStruct(){
        return true;
    }

    public boolean isSameType(Type other){
        if (! other.isStruct()) {
            return false;
        }else{
            return equals(other.getStructType());
        }
    }

    protected void computeOffsets(){
        long offset = 0;
        long bitSize = 32;
        long remainBitSize = 32;
        long maxAlign = 1;
        for (Slot s : members()) {
            offset = AsmUtils.align(offset, s.allocSize());
            s.setOffset(offset);

            offset += s.allocSize();
            maxAlign = Math.max(maxAlign, s.alignment());
        }

        cachedSize = AsmUtils.align(offset, maxAlign);
        cachedAlign = maxAlign;
    }

    public String toString() {
        return "struct " + name;
    }
}
