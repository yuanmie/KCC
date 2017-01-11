package entity;

import asm.ImmediateValue;
import asm.MemoryReference;
import asm.Symbol;

public class ConstantEntry {
    protected String value;
    protected Long s;
    protected Symbol sym;
    protected MemoryReference memref;
    protected ImmediateValue address;

    public ConstantEntry(String val){
        value = val;
    }

    public ConstantEntry(long s){
        this.s = s;
    }

    public String value(){
        return value;
    }

    public Long floatValue(){
        return s;
    }

    public void setSymbol(Symbol sym){
        this.sym = sym;
    }

    public Symbol symbol(){
        if(sym == null){
            throw new Error("must not happen: symbol == null");
        }
        return sym;
    }

    public void setMemref(MemoryReference mem){
        this.memref = mem;
    }

    public MemoryReference memref(){
        if(memref == null){
            throw new Error("must not happen: symbol == null");
        }
        return memref;
    }
    public ImmediateValue address() {
        return address;
    }

    public void setAddress(ImmediateValue imm){
        this.address = imm;
    }
}
