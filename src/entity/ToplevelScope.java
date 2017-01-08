package entity;

import ast.IntegerLiteralNode;

import java.util.*;

public class ToplevelScope extends Scope{
    protected Map<String, Entity> entities;
    protected List<DefinedVariable> staticLocalVariables;

    public ToplevelScope(){
        super();
        entities = new LinkedHashMap<>();
        staticLocalVariables = null;
    }

    public boolean isToplevel(){
        return true;
    }

    @Override
    public ToplevelScope toplevel() {
        return this;
    }


    public Scope parent(){
        return null;
    }

    public void declareEntity(Entity entity){
        Entity e = entities.get(entity.name());
        if(e != null){
            throw new Error("duplicated declaration: " +
            entity.name());
        }
        entities.put(entity.name(), entity);
    }

    public void defineEntity(Entity entity){
        Entity e = entities.get(entity.name());
        if(e != null){
            throw new Error("duplicated declaration: " +
                    entity.name());
        }
        entities.put(entity.name(), entity);
    }

    public Entity get(String name){
        Entity ent = entities.get(name);
        if(ent == null){
            throw new Error("unresolved reference: " + name);
        }

        return ent;
    }

    public List<Variable> allGlobalVariables(){
        List<Variable> result = new ArrayList<Variable>();
        for(Entity ent :entities.values()){
            if(ent instanceof Variable){
                result.add((Variable) ent);
            }
        }

        result.addAll(staticLocalVariables());
        return result;
    }

    public List<DefinedVariable> definedGlobalScopeVariables(){
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for(Entity ent : entities.values()){
            if(ent instanceof DefinedVariable){
                result.add((DefinedVariable)ent);
            }
        }
        result.addAll(staticLocalVariables());
        return result;
    }

    private List<DefinedVariable> staticLocalVariables() {
        if(staticLocalVariables == null){
            staticLocalVariables = new ArrayList<DefinedVariable>();
            for(LocalScope s : children){
                staticLocalVariables.addAll(s.staticLocalVariables());
            }
            Map<String, Integer> seqTable = new HashMap<>();
            for(DefinedVariable var : staticLocalVariables){
                Integer seq = seqTable.get(var.name());
                if(seq == null){
                    var.setSequence(0);
                    seqTable.put(var.name(), 1);
                }else{
                    var.setSequence(seq);
                    seqTable.put(var.name(), seq + 1);
                }
            }
        }
        return staticLocalVariables;
    }


    public void checkReferences() {
        for(Entity ent : entities.values()){
            if(ent.isDefined() && ent.isPrivate()
                    && !ent.isConstant() && !ent.isRefered()){
                System.out.println("unused variable: " + ent.name());
            }
        }

        for(LocalScope funcScope : children){
            for(LocalScope s : funcScope.children){
                s.checkReferences();
            }
        }
    }
}
