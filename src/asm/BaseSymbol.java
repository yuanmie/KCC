package asm;

abstract public  class BaseSymbol implements Symbol{
    public boolean isZero(){
        return false;
    }

    public Literal plus(long n){
        throw new Error("must not happend: BaseSymbol.plus called");
    }
}
