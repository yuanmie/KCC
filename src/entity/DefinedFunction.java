package entity;

import ast.BlockNode;
import ast.DeclarationVisitor;
import ast.FuncallNode;
import ast.TypeNode;
import ir.Stmt;

import java.util.List;

public class DefinedFunction extends Function{
    protected Params params;
    protected BlockNode body;
    protected LocalScope scope;
    protected List<Stmt> ir;

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


    public void setIR(List<Stmt> ir){
        this.ir = ir;
    }

    public List<Stmt> ir(){
        return ir;
    }
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
