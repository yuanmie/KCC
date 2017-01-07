package entity;

import ast.BlockNode;
import ast.FuncallNode;
import ast.TypeNode;

import java.util.List;

public class DefinedFunction extends Function{
    protected Params params;
    protected BlockNode body;

    public DefinedFunction(boolean priv, TypeNode type,
                           String name, Params params,
                           BlockNode body){
        super(priv, type, name);
    }

    @Override
    public List<CBCParameter> parameters() {
        return params.parameters();
    }

    @Override
    public boolean isDefined() {
        return false;
    }

    public BlockNode body(){
        return body;
    }
}
