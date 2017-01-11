package ir;

import asm.Label;

public class Case {
    public long value;
    public Label label;

    public Case(long value, Label label){
        this.value = value;
        this.label = label;
    }
}
