package asm;

public class DirectMemoryReference extends MemoryReference{
    protected Literal value;

    public DirectMemoryReference(Literal val){
        this.value = val;
    }

    public Literal value(){
        return this.value;
    }

    public void fixOffset(long diff){
        throw new Error("DirectMemoryReference fixOffset");
    }

    public String toString(){
        return toSource(SymbolTable.dummy());
    }

    @Override
    public String toSource(SymbolTable table) {
        return this.value.toSource(table);
    }


    @Override
    protected int cmp(DirectMemoryReference mem) {
        return value.compareTo(mem.value);
    }

    @Override
    protected int cmp(IndirectMemoryReference mem) {
        return 1;
    }

    @Override
    public int compareTo(MemoryReference mem) {
        return -(mem.cmp(this));
    }
}
