package asm;

abstract public class Assembly {
    abstract public String toSource(SymbolTable table);

    public boolean isInstruction(){
        return false;
    }

    public boolean isLabel(){
        return false;
    }

    public boolean isDirective(){
        return false;
    }

    public boolean isComment(){
        return false;
    }

    public void collectStatistics(){}

}
