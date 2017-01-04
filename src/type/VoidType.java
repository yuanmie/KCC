package type;

import static sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte1.other;

public class VoidType extends Type {

    public VoidType() {

    }

    public boolean isVoid() {
        return true;
    }


    @Override
    public long size() {
        return 1;
    }

    public boolean equals(Object other){
        return (other instanceof VoidType);
    }

    @Override
    public boolean isSameType(Type other) {
       return other.isVoid();
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isVoid();
    }

    @Override
    public boolean isCastableTo(Type target) {
        return target.isVoid();
    }
}
