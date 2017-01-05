package ast;

import type.Type;

public class VariableNode extends LHSNode{
    private String name;

    public VariableNode(String name){
        this.name = name;
    }

    public String name(){
        return name;
    }

    @Override
    protected Type origType() {
        return null;
    }
}
