package entity;

import asm.ImmediateValue;
import asm.MemoryReference;
import asm.Operand;
import ast.ExprNode;
import ast.TypeNode;
import compiler.TypeResolver;
import type.Type;

abstract public class Entity {
    protected String name;
    protected boolean isPrivate;
    protected TypeNode typeNode;
    protected long nRefered;
    protected MemoryReference memref;
    protected Operand address;

    public Entity(boolean priv, TypeNode type, String name){
        this.name = name;
        this.isPrivate = priv;
        this.typeNode = type;
        this.nRefered = 0;
    }

    public String name(){
        return name;
    }

    public String symbolString(){
        return name();
    }

    abstract public boolean isDefined();
    abstract public boolean isInitialized();

    public boolean isConstant(){
        return false;
    }

    public ExprNode value(){
        throw new Error("Entity value");
    }

    public boolean isParameter(){
        return false;
    }

    public boolean isPrivate(){
        return isPrivate;
    }

    public TypeNode typeNode(){
        return typeNode;
    }

    public Type type(){
        return typeNode.type();
    }

    public long allocSize(){
        return type().allocSize();
    }

    public long alignment(){
        return type().alignment();
    }

    public boolean isRefered(){
        return (nRefered > 0);
    }

    public boolean isVariadic(){
        return false;
    }

    public  void refered(){
        nRefered++;
    }

    public void setMemref(MemoryReference mem){
        this.memref = mem;
    }

    public MemoryReference memref(){
        checkAddress();
        return memref;
    }

    public void setAddress(MemoryReference mem){
        this.address = mem;
    }

    public void setAddress(ImmediateValue imm){
        this.address = imm;
    }

    public Operand address(){
        checkAddress();
        return address;
    }

    protected void checkAddress(){
        if(memref == null && address == null){
            throw new Error("address did not resolved: " + name);
        }
    }

    abstract public <T> T accept(EntityVisitor<T> visitor);


}
