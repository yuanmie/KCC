package ir;


import asm.Type;

abstract class Expr {
    final Type type;
    protected boolean isArg = false;

    public boolean isArg(){
        return isArg;
    }

    public void setArg(boolean arg){
        isArg = arg;
    }

    public Expr(Type type){
        this.type = type;
    }

    public Type type(){
        return type;
    }

    public boolean isVar(){
        return false;
    }

    public boolean isAddr(){
        return false;
    }

    public boolean isConstant(){
        return false;
    }

}
