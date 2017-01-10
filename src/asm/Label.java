package asm;

public class Label extends Assembly{
    protected Symbol symbol;

    public Label(){
        this(new UnnamedSymbol());
    }

    public Label(Symbol sym){
        this.symbol = sym;
    }

    public Symbol symbol(){
        return symbol;
    }

    public boolean isLabel(){
        return true;
    }

    @Override
    public String toSource(SymbolTable table) {
        return null;
    }
}
