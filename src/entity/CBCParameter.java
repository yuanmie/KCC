package entity;

import ast.TypeNode;

public class CBCParameter extends DefinedVariable{
    public CBCParameter(TypeNode type, String name){
        super(false, type, name, null);
    }

    public boolean isParameter(){
        return true;
    }
}
