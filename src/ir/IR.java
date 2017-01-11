package ir;

import entity.*;

import javax.management.timer.TimerMBean;
import java.util.ArrayList;
import java.util.List;

public class IR {
    List<DefinedVariable> defvars;
    List<DefinedFunction> defuns;
    List<UndefinedFunction> funcdecls;
    ToplevelScope scope;
    ConstantTable constantTable;
    List<DefinedVariable> gvars;
    List<DefinedVariable> comms;

    public IR(List<DefinedVariable> defvars,
              List<DefinedFunction> defuns,
              List<UndefinedFunction> funcdecls,
              ToplevelScope scope,
              ConstantTable constantTable) {
        this.defvars = defvars;
        this.defuns = defuns;
        this.funcdecls = funcdecls;
        this.scope = scope;
        this.constantTable = constantTable;
    }

    public String fileName(){
        return "helloworld";
    }

    public List<DefinedVariable> definedVariables(){
        return defvars;
    }

    public boolean isFunctionDefined(){
        return !defuns.isEmpty();
    }

    public List<DefinedFunction> definedFunctions(){
        return defuns;
    }

    public ToplevelScope scope(){
        return scope;
    }

    public List<Function> allFunctions(){
        List<Function> result = new ArrayList<>();
        result.addAll(defuns);
        result.addAll(funcdecls);
        return result;
    }

    public List<Variable> allGlobalVariables(){
        return scope().allGlobalVariables();
    }

    public boolean isGlobalVariableDefined(){
        return !definedGlobalVariables().isEmpty();
    }

    private List<DefinedVariable> definedGlobalVariables() {
        if(gvars == null){
            initVariables();
        }
        return gvars;
    }

    public boolean isCommonSymbolDefined(){
        return !definedCommonSymbols().isEmpty();
    }

    private List<DefinedVariable> definedCommonSymbols() {
        if(comms == null){
            initVariables();
        }
        return comms;
    }

    private void initVariables() {
        gvars = new ArrayList<>();
        comms = new ArrayList<>();
        for(DefinedVariable var : scope().definedGlobalScopeVariables()){
            (var.hasInitializer() ? gvars : comms).add(var);
        }
    }

    public boolean isStringLiteralDefined(){
        return !constantTable.isEmpty();
    }

    public ConstantTable constantTable(){
        return constantTable;
    }

}
