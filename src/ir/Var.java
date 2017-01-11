package ir;

import asm.MemoryReference;
import asm.Operand;
import asm.Type;
import entity.Entity;

public class Var extends Expr{
    protected Entity entity;

    public Var(Type type, Entity entity){
        super(type);
        this.entity = entity;
    }

    public boolean isVar(){
        return true;
    }

    public Type type(){
        if(super.type() == null){
            System.out.println("var is too big to load 1 insn");
            return null;
        }

        return super.type();
    }

    public String name(){
        return entity.name();
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

    public Addr addressNode(Type type){
        return new Addr(type, entity);
    }

    public Entity getEntityForce(){
        return entity;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
