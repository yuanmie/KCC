package compiler;

import ast.*;
import entity.*;

import java.util.LinkedList;
import java.util.List;

public class LocalResolver extends Visitor{
    private final LinkedList<Scope> scopeStack;
    private final ConstantTable constantTable;

    public LocalResolver(){
        this.scopeStack = new LinkedList<>();
        this.constantTable = new ConstantTable();
    }

    private void resolve(StmtNode n){
        n.accept(this);
    }

    private void resolve(ExprNode n){
        n.accept(this);
    }

    public void resolve(AST ast) {
        ToplevelScope toplevel = new ToplevelScope();
        scopeStack.add(toplevel);

        for(Entity decl : ast.declarations()){
            toplevel.declareEntity(decl);
        }

        for(Entity ent : ast.definitions()){
            toplevel.defineEntity(ent);
        }

        resolveGvarInitializers(ast.definedVariables());
        resolveConstantValues(ast.constants());
        resolveFunctions(ast.definedFunctions());

        toplevel.checkReferences();

        ast.setScope(toplevel);
        ast.setConstantTable(constantTable);
    }

    private void resolveFunctions(List<DefinedFunction> definedFunctions) {
        for(DefinedFunction func : definedFunctions){
            pushScope(func.parameters());
            resolve(func.body());
            func.setScope(popScope());
        }
    }

    private LocalScope popScope() {
        return (LocalScope)scopeStack.removeLast();
    }

    private void pushScope(List<? extends DefinedVariable> vars) {
        LocalScope scope = new LocalScope(currentScope());
        for(DefinedVariable var : vars){
            if(scope.isDefinedLocally(var.name())){
                throw new Error("duplicated variable in scope: " + var.name());
            }else{
                scope.defineVariable(var);
            }
        }

        scopeStack.addLast(scope);
    }

    private Scope currentScope() {
        return scopeStack.getLast();
    }

    private void resolveConstantValues(List<Constant> constants) {
        for(Constant c : constants){
            resolve(c.value());
        }
    }

    private void resolveGvarInitializers(List<DefinedVariable> gvars){
        for(DefinedVariable gvar : gvars){
            if(gvar.hasInitializer()){
                resolve(gvar.initializer());
            }
        }
    }


    public Void visit(BlockNode node){
        pushScope(node.variables());
        super.visit(node);
        node.setScope(popScope());
        return null;
    }

    public Void visit(StringLiteralNode node){
        node.setEntry(constantTable.intern(node.value()));
        return null;
    }
    @Override
    public Void visit(CommaNode commaNode) {
        return null;
    }

    @Override
    public Void visit(FloatLiteralNode node) {
        node.setEntry(constantTable.intern(node.rawValue()));
        return null;
    }

    //resolve init node
    @Override
    public Void visit(InitNode initNode) {
       for(ExprNode e : initNode.getInits()){
           e.accept(this);
       }
       return null;
    }

    public Void visit(VariableNode node){
        try{
            Entity ent = currentScope().get(node.name());
            ent.refered();
            node.setEntity(ent);
        }catch (Error e){
            System.out.println(node.name() + " : " + e.getMessage());
        }
        return null;
    }
}
