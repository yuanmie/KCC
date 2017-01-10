package asm;

public class FloatLiteral implements Literal{
    protected double value;

    public FloatLiteral(double n){
        this.value = n;
    }

    public boolean equals(Object other){
        return (other instanceof FloatLiteral)
                && equals((FloatLiteral)other);
    }

    public boolean equals(FloatLiteral other){
        return other.value == this.value;
    }

    public double value(){
        return this.value;
    }

    public boolean isZero(){
        return value == 0;
    }

    public FloatLiteral plus(long diff){
        return new FloatLiteral((value + diff));
    }

    public int cmp(IntegerLiteral i){
        return 0;
    }

    public FloatLiteral floatLiteral(){
        return this;
    }

    public String toSource(){
        return new Double(value).toString();
    }

    @Override
    public String toSource(SymbolTable table) {
        return toSource();
    }

    public String toString(){
        return new Double(value).toString();
    }

    public int compareTo(Literal lit){
        return -(lit.cmp(this));
    }

    public int cmp(FloatLiteral i){
        return new Double(value).compareTo(new Double(i.value));
    }

    public int cmp(NamedSymbol sym){
        return -1;
    }

    public int cmp(UnnamedSymbol sym){
        return -1;
    }

    public int cmp(SuffixedSymbol sym){
        return -1;
    }
}
