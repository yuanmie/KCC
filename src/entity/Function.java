package entity;

import asm.Label;
import asm.Symbol;
import ast.TypeNode;
import type.Type;

import java.util.List;

abstract public class Function extends Entity{
    protected Symbol callingSymbol;
    protected Label label;

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

    public void setCallingSymbol(Symbol sym){
        if(this.callingSymbol != null){
            throw new Error("must not happen: Function callingSymbol was set again");
        }
        this.callingSymbol = sym;
    }

    public Symbol callingSymbol(){
        if(this.callingSymbol == null){
            throw new Error("must not happen: Function callingSymbol called but not null");
        }
        return this.callingSymbol;
    }

    public Label label(){
        if(label != null){
            return label;
        }else{
            return label = new Label(callingSymbol);
        }
    }
}
