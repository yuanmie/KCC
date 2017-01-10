package asm;

import ast.IntegerLiteralNode;

public interface Literal extends Comparable<Literal>{
    public String toSource();
    public String toSource(SymbolTable table);
    public boolean isZero();
    public Literal plus(long diff);
    public int cmp(IntegerLiteral i);
    public int cmp(FloatLiteral f);
    public int cmp(NamedSymbol sym);
    public int cmp(UnnamedSymbol sym);
    public int cmp(SuffixedSymbol sym);
}
