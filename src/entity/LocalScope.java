package entity;

import type.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalScope extends Scope{
    protected Scope parent;
    protected Map<String, DefinedVariable> variables;

    public LocalScope(Scope parent){
        super();
        this.parent = parent;
        parent.addChild(this);
        variables = new LinkedHashMap<String, DefinedVariable>();
    }

    public boolean isToplevel(){
        return false;
    }

    public ToplevelScope toplevel(){
        return parent.toplevel();
    }

    public Scope parent(){
        return this.parent;
    }

    public List<LocalScope> children(){
        return children;
    }

    public boolean isDefinedLocally(String name){
        return variables.containsKey(name);
    }

    public void defineVariable(DefinedVariable var){
        if(variables.containsKey(var.name())){
            throw new Error("duplicated variable: " + var.name());
        }

        variables.put(var.name(), var);
    }

    public DefinedVariable allocateTmp(Type t){
        DefinedVariable var = DefinedVariable.tmp(t);
        defineVariable(var);
        return var;
    }

    public Entity get(String name){
        DefinedVariable var = variables.get(name);
        if(var != null){
            return var;
        }else{
            return parent.get(name);
        }
    }


    public List<DefinedVariable> allLocalVariables(){
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for(LocalScope s : allLocalScope()){
            result.addAll(s.localVariables());
        }

        return result;
    }

    public List<DefinedVariable> localVariables(){
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for(DefinedVariable var : variables.values()){
            if(!var.isPrivate()){
                result.add(var);
            }
        }

        return result;
    }

    public List<DefinedVariable> staticLocalVariables(){
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for(LocalScope s: allLocalScope()){

        }
    }

    protected List<LocalScope> allLocalScope(){
        List<LocalScope> result = new ArrayList<LocalScope>();
        collectScope(result);
        return result;
    }

    protected void collectScope(List<LocalScope> buf){
        buf.add(this);
        for(LocalScope s : children){
            s.collectScope(buf);
        }
    }

}
