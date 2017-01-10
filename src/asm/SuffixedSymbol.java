package asm;

public class SuffixedSymbol implements Symbol{
    protected Symbol base;
    protected String suffix;

    public SuffixedSymbol(Symbol base, String suffix){
        this.base = base;
        this.suffix = suffix;
    }
    @Override
    public String name() {
        return base.name();
    }

    @Override
    public String toSource() {
        return base.toSource()+suffix;
    }

    @Override
    public String toSource(SymbolTable table) {
        return base.toSource(table);
    }

    public String toString(){
        return base.toString() + suffix;
    }
    @Override
    public boolean isZero() {
        return false;
    }

    @Override
    public Literal plus(long n) {
        throw new Error("must not happen : SuffixdSymbol.plus called");
    }

    @Override
    public int cmp(IntegerLiteral i) {
        return 1;
    }

    @Override
    public int cmp(FloatLiteral f) {
        return 1;
    }

    @Override
    public int cmp(NamedSymbol sym) {
        return toString().compareTo(sym.toString());
    }

    @Override
    public int cmp(UnnamedSymbol sym) {
        return -1;
    }

    @Override
    public int cmp(SuffixedSymbol sym) {
        return toString().compareTo(sym.toString());
    }

    @Override
    public int compareTo(Literal lit) {
        return -(lit.compareTo(this));
    }
}
