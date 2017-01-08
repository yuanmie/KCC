package entity;

import ast.BlockNode;
import ast.DeclarationVisitor;
import ast.FuncallNode;
import ast.TypeNode;

import java.util.List;

public class DefinedFunction extends Function{
    protected Params params;
    protected BlockNode body;
    protected LocalScope scope;

    public DefinedFunction(boolean priv, TypeNode type,
                           String name, Params params,
                           BlockNode body){
        super(priv, type, name);
        this.params = params;
        this.body = body;
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

    public void setScope(LocalScope scope){
        this.scope = scope;
    }

    public LocalScope lvarScope(){
        return body().scope();
    }

    public boolean isVariadic(){
        return params.isVararg();
    }

    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
