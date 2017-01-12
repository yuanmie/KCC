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
        try{
            return type().isCallable();
        }catch (Error e){
            return false;
        }

    }

    public boolean isPointer(){
        try{
            return type().isPointer();
        }catch(Error e){
            return false;
        }
    }

    abstract public <S,E> E accept(ASTVisitor<S,E> visitor);

    public long allocSize() {
        return type().allocSize();
    }
}
