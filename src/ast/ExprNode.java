package ast;


import type.Type;

abstract public class ExprNode extends Node{
    public ExprNode(){
        super();
    }

    abstract public Type type();

    protected Type origType(){
        return type();
    }

    public boolean isConstant(){
        return false;
    }

    public boolean isParameter(){
        return false;
    }

    public boolean isLvalue(){
        return false;
    }

    public boolean isAssignable(){
        return false;
    }

    public boolean isLoadable(){
        return false;
    }

    public boolean isCallable(){
        //dosomething
        return false;
    }

    public boolean isPointer(){
        //dosomething
        return false;
    }

}
