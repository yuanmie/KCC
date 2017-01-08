package ast;

import entity.DefinedVariable;
import entity.Entity;
import type.Type;

public class VariableNode extends LHSNode{
    private String name;
    private Entity entity;

    public VariableNode(String name){
        this.name = name;
    }

    public VariableNode(DefinedVariable var){
        this.entity = var;
        this.name = var.name();
    }

    public String name(){
        return name;
    }

    public boolean isResolved(){
        return entity != null;
    }

    public boolean isLvalue(){
        if(entity.isConstant()){
            return false;
        }
        return true;
    }

    public boolean isAssignable(){
        if(entity.isConstant()){
            return false;
        }
        return isLoadable();
    }

    public boolean isParameter(){
        return entity().isParameter();
    }

    public TypeNode typeNode(){
        return entity().typeNode();
    }
    @Override
    protected Type origType() {
        return entity().type();
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
