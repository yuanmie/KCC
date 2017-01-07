package entity;

import ast.ExprNode;
import ast.TypeNode;
import type.Type;

public class DefinedVariable extends Variable{
    protected ExprNode initializer;

    protected long sequence;

    public DefinedVariable(boolean priv, TypeNode type,
                           String name, ExprNode init){
        super(priv, type, name);
        initializer = init;
        sequence = -1;
    }

    static long tmpSeq = 0;

    static public DefinedVariable tmp(Type t){
        return new DefinedVariable(false, new TypeNode(t),
                "@tmp" + tmpSeq++, null);
    }

    public boolean isDefined(){
        return true;
    }

    @Override
    public boolean isInitialized() {
        return hasInitializer();
    }

    public void setSequence(long seq){
        this.sequence = seq;
    }

    public boolean hasInitializer(){
        return (initializer != null);
    }

    public ExprNode initializer(){
        return initializer;
    }

    public void setInitializer(ExprNode expr){
        this.initializer = expr;
    }


}
