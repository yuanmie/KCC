package entity;

public class ConstantEntry {
    protected String value;
    protected Long s;

    public ConstantEntry(String val){
        value = val;
    }

    public ConstantEntry(long s){
        this.s = s;
    }

    public String value(){
        return value;
    }

    public Long floatValue(){
        return s;
    }
}
