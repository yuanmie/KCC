package asm;

public class ImmediateValue extends Operand {

    protected Literal expr;
    private boolean isFloat = false;

    public ImmediateValue(long n){
        this(new IntegerLiteral(n));
    }

    public ImmediateValue(Literal expr){
        this.expr = expr;
    }

    public boolean equals(Object other){
        if(!(other instanceof ImmediateValue)){
            return false;
        }
        ImmediateValue imm = (ImmediateValue)other;
        return expr.equals(imm.expr);
    }

    public Literal expr(){
        return this.expr;
    }
    @Override
    public String toSource(SymbolTable table) {
        if(isFloat){
            return expr.toSource();
        }else{
            return "$" + expr.toSource(table);
        }
    }

    public void setIsFloat(boolean isFloat){
        this.isFloat = isFloat;
    }
}
