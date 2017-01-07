package entity;

import ast.TypeNode;

import java.util.List;

public class UndefinedFunction extends Function{
    protected Params params;
    public UndefinedFunction(TypeNode t, String name, Params params){
        super(false, t, name);
        this.params = params;
    }

    public List<CBCParameter> parameters(){
        return params.parameters();
    }

    public boolean isVariadic(){
        return params.isVararg();
    }

    public boolean isDefined(){
        return false;
    }
}
