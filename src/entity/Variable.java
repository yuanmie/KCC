package entity;

import ast.TypeNode;
abstract  class Variable extends Entity{
    public Variable(boolean priv, TypeNode type, String name){
        super(priv, type,name);
    }
}
