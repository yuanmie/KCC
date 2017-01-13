package asm;

public class IndirectMemoryReference extends MemoryReference{
    Literal offset;
    Register base;
    boolean fixed;

    public IndirectMemoryReference(long offset, Register base){
        this(new IntegerLiteral(offset), base, true);
    }

    public IndirectMemoryReference(Symbol offset, Register base){
        this(offset, base, true);
    }

    public IndirectMemoryReference(Literal offset, Register base, boolean b) {
        this.offset = offset;
        this.base = base;
        this.fixed = fixed;
    }

    public Literal offset(){
        return offset;
    }


    @Override
    public void fixOffset(long diff) {
        if(fixed){
            throw new Error("must not happen: fixed = true");
        }
        long curr = ((IntegerLiteral)offset).value;
        this.offset = new IntegerLiteral(curr + diff);
        this.fixed = true;
    }

    public Register base(){
        return base;
    }

    public String toString(){
        return toSource(SymbolTable.dummy());
    }

    public String toSource(SymbolTable table){
        if(!fixed){
            throw new Error("must not happend: writeing unfixed variable");
        }
        return (offset.isZero() ? "" : offset.toSource(table))
                + "(" + base.toSource(table) + ")";
    }
    @Override
    protected int cmp(DirectMemoryReference mem) {
        return -1;
    }

    @Override
    protected int cmp(IndirectMemoryReference mem) {
        return offset.compareTo(mem.offset);
    }

    @Override
    public int compareTo(MemoryReference mem) {
        return -(mem.compareTo(this));
    }

    public static IndirectMemoryReference relocatable(long offset, Register base) {
        return new IndirectMemoryReference(new IntegerLiteral(offset), base, false);
    }
}
