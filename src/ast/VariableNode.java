package ast;

import entity.Entity;
import type.Type;

public class VariableNode extends LHSNode{
    private String name;
    private Entity entity;

    public VariableNode(String name){
        this.name = name;
    }

    public String name(){
        return name;
    }

    @Override
    protected Type origType() {
        return null;
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public Entity entity() {
        if (entity == null) {
            throw new Error("VariableNode.entity == null");
        }
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
