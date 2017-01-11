package ir;

import asm.MemoryReference;
import asm.Operand;
import asm.Type;
import entity.Entity;

public class Addr extends Expr{

    Entity entity;

    public Addr(Type type, Entity entity){
        super(type);
        this.entity = entity;
    }

    public boolean isAddr(){
        return true;
    }

    public Entity entity(){
        return entity;
    }

    public Operand address(){
        return entity.address();
    }

    public MemoryReference memref(){
        return entity.memref();
    }

    public Entity getEntityForce(){
        return entity;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
