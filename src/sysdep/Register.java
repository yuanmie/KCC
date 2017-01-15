package sysdep;

import asm.SymbolTable;
import asm.Type;

public class Register extends asm.Register{
    RegisterClass _class;
    Type type;
    public Register(RegisterClass _class, Type type) {
        this._class = _class;
        this.type = type;
    }

    Register forType(Type t){
        return new Register(_class, t);
    }

    public boolean isRegister(){
        return true;
    }

    public boolean equals(Object other){
        return (other instanceof Register) && equals((Register)other);
    }

    public boolean equals(Register reg){
        return _class.equals(reg._class);
    }

    public int hashcode(){
        return _class.hashCode();
    }

    RegisterClass registerClass(){
        return _class;
    }


    String baseName(){
        return _class.toString().toLowerCase();
    }
    @Override
    public String toSource(SymbolTable table) {
        return "%" + typedName();
    }

    private String typedName() {
        switch (type){
            case INT8:return lowerByteRegister();
            case INT16:return baseName();
            case INT32:return "e" + baseName();
            case INT64:return "r" + baseName();
            case Float32:return "e" + baseName();
            default:
                throw new Error("Unknown register Type: " + type);
        }
    }

    private String lowerByteRegister(){
        switch (_class){
            case AX:
            case BX:
            case CX:
            case DX:
                return baseName().substring(0,1) + "l";
            default:
                throw new Error("does not have lower-byte regiseter: " + _class);
        }
    }


}
