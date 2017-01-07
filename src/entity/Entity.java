package entity;

import ast.ExprNode;
import ast.TypeNode;
import type.Type;

abstract class Entity {
    protected String name;
    protected boolean isPrivate;
    protected TypeNode typeNode;
    protected long nRefered;

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
}
