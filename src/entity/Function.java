package entity;

import ast.TypeNode;
import type.Type;

import java.util.List;

abstract public class Function extends Entity{
    public Function(boolean priv, TypeNode t, String name){
        super(priv, t, name);
    }

    public boolean isInitialized(){
        return true;
    }

    abstract public boolean isDefined();
    abstract public List<CBCParameter> parameters();

    public Type returnType(){
        return type().getFunctionType().returnType();
    }

    public boolean isVoid(){
        return returnType().isVoid();
    }
}
