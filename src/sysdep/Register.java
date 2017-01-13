package sysdep;

import asm.SymbolTable;
import asm.Type;

public class Register extends asm.Register{
    Type type;
    public Register(RegisterClass bp, Type natualType) {

    }

    @Override
    public String toSource(SymbolTable table) {
        return null;
    }
}
