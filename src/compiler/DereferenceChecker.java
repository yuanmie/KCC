package compiler;

import ast.*;
import entity.DefinedFunction;
import entity.DefinedVariable;
import type.CompositeType;
import type.Type;
import type.TypeTable;

public class DereferenceChecker extends Visitor{

    private final TypeTable typeTable;
    public DereferenceChecker(TypeTable types) {
        this.typeTable = types;
    }


    @Override
    public Void visit(CommaNode commaNode) {
        return null;
    }

    @Override
    public Void visit(FloatLiteralNode floatLiteralNode) {
        return null;
    }

    @Override
    public Void visit(InitNode initNode) {
        return null;
    }

    public Void visit(CastNode node){
        super.visit(node);
        if(node.type().isArray()){
            throw new Error("cast specifies array type");
        }
        return null;
    }
    public void check(AST ast) {
        for(DefinedVariable var : ast.definedVariables()){
            checkToplevelVariable(var);
        }

        for(DefinedFunction f : ast.definedFunctions()){
            check(f.body());
        }
    }

    private void checkToplevelVariable(DefinedVariable var) {
        checkVariable(var);
        if(var.hasInitializer()){
            checkConstant(var.initializer());
        }
    }

    private void checkVariable(DefinedVariable var) {
        if(var.hasInitializer()){
            try{
                check(var.initializer());
            }catch (Error e){
                System.out.println("initializer error");
            }
        }
    }

    private void checkConstant(ExprNode expr) {
        if(!expr.isConstant()){
            throw new Error(expr.toString() + "not a constant");
        }
    }

    private void check(StmtNode node){
        node.accept(this);
    }

    private void check(ExprNode node){
        node.accept(this);
    }

    public Void visit(BlockNode node){
        for(DefinedVariable var : node.variables()){
            checkVariable(var);
        }

        for(StmtNode stmt : node.stmts()){
            try{
                check(stmt);
            }catch (Error e){
                System.out.println("stmt check " + e.getMessage());
            }
        }

        return null;
    }

    public Void visit(AssignNode node){
        super.visit(node);
        checkAssignment(node);
        return null;
    }

    public Void visit(OpAssignNode node){
        super.visit(node);
        checkAssignment(node);
        return null;
    }

    private void checkAssignment(AbstractAssignNode node) {
        if(!node.lhs().isAssignable()){
            throw new Error("invalid lhs expression");
        }
    }

    public Void visit(PrefixOpNode node){
        super.visit(node);
        if(!node.expr().isAssignable()){
            throw new Error("cannot increment/decrement");
        }
        return null;
    }

    public Void visit(SuffixOpNode node){
        super.visit(node);
        if(!node.expr().isAssignable()){
            throw new Error("cannot increment/decrement");
        }

        return null;
    }

    public Void visit(FuncallNode node){
        super.visit(node);
        if(!node.expr().isCallable()){
            throw new Error("calling object is not a function");
        }
        return null;
    }

    public Void visit(ArefNode node){
        super.visit(node);
        if(!node.expr().isPointer()){
            throw new Error("indexing non-array/pointer expression");
        }
        handleImplicitAddress(node);
        return null;
    }

    public Void visit(MemberNode node){
        super.visit(node);
        checkMemberRef(node.expr().type(), node.member());
        handleImplicitAddress(node);
        return null;
    }

    public Void visit(PtrMemberNode node){
        super.visit(node);
        if(!node.expr().isPointer()){
            throw new Error("dereferencing non-pointer expression");
        }
        checkMemberRef(node.dereferdType(), node.member());
        handleImplicitAddress(node);
        return null;
    }

    public Void visit(DereferenceNode node){
        super.visit(node);
        if(!node.expr().isPointer()){
            throw new Error("dereferencing non-pointer expression");
        }
        handleImplicitAddress(node);
        return null;
    }

    public Void visit(AddressNode node){
        super.visit(node);
        if(!node.expr().isLvalue()){
            throw new Error("invalid expression for &");
        }
        Type base = node.expr().type();
        if(!node.expr().isLoadable()){
            node.setType(base);
        }else{
            node.setType(typeTable.pointerTo(base));
        }
        return null;
    }

    public Void visit(VariableNode node){
        super.visit(node);
        if(node.entity().isConstant()){
            checkConstant(node.entity().value());
        }
        handleImplicitAddress(node);
        return null;
    }

    private void checkMemberRef(Type t, String member) {
        if(!t.isCompositeType()){
            throw new Error("accessing member`" + member + "` for non-struct/union: " + t);
        }
        CompositeType type = t.getCompositeType();
        if(!type.hasMember(member)){
            throw new Error(type.toString() + " does not have member: " + member);
        }
    }

    private void handleImplicitAddress(LHSNode node) {
        if(!node.isLoadable()){
            Type t = node.type();
            if(t.isArray()){
                node.setType(typeTable.pointerTo(t.baseType()));
            }else{
                node.setType(typeTable.pointerTo(t));
            }
        }
    }
}
