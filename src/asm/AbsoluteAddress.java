package asm;

public class AbsoluteAddress extends Operand{
    protected Register regiter;

    public AbsoluteAddress(Register reg){
        this.regiter = reg;
    }

    public Operand register(){
        return this.regiter;
    }

    public String toSource(SymbolTable table){
        return "*" + regiter.toSource(table);
    }

}
