package asm;

abstract public class Register extends Operand {

    public boolean isRegister(){
        return true;
    }

    @Override
    abstract public String toSource(SymbolTable table);
}
