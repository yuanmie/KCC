package ast;

import entity.*;
import ir.IR;

import java.util.ArrayList;
import java.util.List;

public class AST extends Node{
    protected Declarations declarations;
    protected ToplevelScope scope;
    protected ConstantTable constantTable;

    public AST(Declarations declarations){
        super();
        this.declarations = declarations;
    }

    public List<TypeDefinition> types(){
        List<TypeDefinition> result = new ArrayList<>();
        result.addAll(declarations.defstructs);
        result.addAll(declarations.defunions);
        result.addAll(declarations.typedefs());
        return result;
    }

    public List<Entity> entities(){
        List<Entity> result = new ArrayList<>();
        result.addAll(declarations.funcdecls);
        result.addAll(declarations.vardecls);
        result.addAll(declarations.defvars);
        result.addAll(declarations.defuns);
        result.addAll(declarations.constants);
        return result;
    }

    public List<Entity> declarations(){
        List<Entity> result = new ArrayList<>();
        result.addAll(declarations.funcdecls);
        result.addAll(declarations.vardecls);
        return result;
    }

    public List<Entity> definitions(){
        List<Entity> result = new ArrayList<>();
        result.addAll(declarations.defvars);
        result.addAll(declarations.defuns);
        result.addAll(declarations.constants);
        return result;
    }

    public List<Constant> constants(){
        return declarations.constants();
    }

    public List<DefinedVariable> definedVariables() {
        return declarations.defvars();
    }

    public List<DefinedFunction> definedFunctions() {
        return declarations.defuns();
    }


    public void setScope(ToplevelScope scope) {
        if (this.scope != null) {
            throw new Error("must not happen: ToplevelScope set twice");
        }
        this.scope = scope;
    }

    public ToplevelScope scope() {
        if (this.scope == null) {
            throw new Error("must not happen: AST.scope is null");
        }
        return scope;
    }


    public void setConstantTable(ConstantTable table) {
        if (this.constantTable != null) {
            throw new Error("must not happen: ConstantTable set twice");
        }
        this.constantTable = table;
    }

    public ConstantTable constantTable() {
        if (this.constantTable == null) {
            throw new Error("must not happen: AST.constantTable is null");
        }
        return constantTable;
    }

    public IR ir(){
        return new IR(declarations.defvars(),
                declarations.defuns(),
                declarations.funcdecls(),
                scope,
                constantTable);
    }

}
