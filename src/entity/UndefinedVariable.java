package entity;

import ast.TypeNode;

public class UndefinedVariable extends Variable{
    public UndefinedVariable(TypeNode t, String name){
        super(false, t, name);
    }

    public boolean isDefined(){
        return false;
    }

    public boolean isPrivate(){
        return false;
    }

    public boolean isInitialized(){
        return false;
    }

    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
