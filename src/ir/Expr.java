package ir;


import asm.ImmediateValue;
import asm.MemoryReference;
import asm.Operand;
import asm.Type;
import entity.Entity;

abstract public class Expr {
    final Type type;
    protected boolean isArg = false;

    public boolean isArg(){
        return isArg;
    }

    public void setArg(boolean arg){
        isArg = arg;
    }

    public Expr(Type type){
        this.type = type;
    }

    public Type type(){
        return type;
    }

    public boolean isVar(){
        return false;
    }

    public boolean isAddr(){
        return false;
    }

    public boolean isConstant(){
        return false;
    }

    public ImmediateValue asmValue(){
        throw new Error("Expr asmValue called");
    }

    public Operand address(){
        throw new Error("Expr memref called");
    }

    public MemoryReference memref(){
        throw new Error("Expr memref called");
    }

    public Expr addressNode(Type type){
        throw new Error("unexpect node for LHS: " + getClass());
    }

    public Entity getEntityForce(){
        return null;
    }

    abstract public <S, E> E accept(IRVisitor<S, E> visitor);

}
