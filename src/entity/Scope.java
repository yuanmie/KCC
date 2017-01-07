package entity;

import java.util.ArrayList;
import java.util.List;

abstract class Scope {
    protected List<LocalScope> children;

    public Scope(){
        children = new ArrayList<LocalScope>();
    }

    abstract public boolean isToplevel();
    abstract public ToplevelScope toplevel();
    abstract public Scope parent();

    protected void addChild(LocalScope s){
        children.add(s);
    }

    abstract public Entity get(String name);
}
