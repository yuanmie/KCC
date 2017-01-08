package compiler;

import ast.*;
import entity.*;
import type.CompositeType;
import type.Type;
import type.TypeTable;

import java.util.List;

public class TypeResolver extends Visitor implements EntityVisitor<Void>, DeclarationVisitor<Void>
{
    private final TypeTable typeTable;

    public TypeResolver(TypeTable typeTable){
        this.typeTable = typeTable;
    }

    public void resolve(AST ast) {
        defineTypes(ast.types());
        for(TypeDefinition t : ast.types()){
            t.accept(this);
        }

        for(Entity e : ast.entities()){
            e.accept(this);
        }
    }

    private void defineTypes(List<TypeDefinition> deftypes) {
        for(TypeDefinition def : deftypes){
            if(typeTable.isDefined(def.typeRef())){
                System.out.println("duplicated type definition: " + def.typeRef());
            }else{
                typeTable.put(def.typeRef(), def.definingType());
            }
        }
    }

    private void bindType(TypeNode n){
        if(n.isResolved()){
            return;
        }
        n.setType(typeTable.get(n.typeRef()));
    }

    @Override
    public Void visit(DefinedVariable var) {
        bindType(var.typeNode());
        if(var.hasInitializer()){
            visitExpr(var.initializer());
        }
        return null;
    }

    @Override
    public Void visit(UndefinedVariable var) {
        bindType(var.typeNode());
        return null;
    }

    @Override
    public Void visit(DefinedFunction func) {
        bindType(func.typeNode());
        resolveFunctionHeader(func);
        visitStmt(func.body());
        return null;
    }

    private void resolveFunctionHeader(Function func) {
        bindType(func.typeNode());
        for(CBCParameter param : func.parameters()){
            Type t  = typeTable.getParamType(param.typeNode().typeRef());
            param.typeNode().setType(t);
        }
    }

    @Override
    public Void visit(UndefinedFunction func) {
        resolveFunctionHeader(func);
        return null;
    }

    @Override
    public Void visit(Constant c) {
        bindType(c.typeNode());
        visitExpr(c.value());
        return null;
    }

    @Override
    public Void visit(StructNode struct) {
        resolveCompositeType(struct);
        return null;
    }

    private void resolveCompositeType(CompositeTypeDefinition def) {
        CompositeType ct = (CompositeType)typeTable.get(def.typeNode().typeRef());
        if(ct == null){
            throw new Error("cannot intern struct/union: " + def.name());
        }

        for(Slot s : ct.members()){
            bindType(s.typeNode());
        }
    }

    @Override
    public Void visit(UnionNode union) {
        resolveCompositeType(union);
        return null;
    }

    @Override
    public Void visit(TypedefNode typedef) {
        bindType(typedef.typeNode());
        bindType(typedef.realTypeNode());
        return null;
    }

    public Void visit(BlockNode node){
        for(DefinedVariable var : node.variables()){
            var.accept(this);
        }
        visitStmts(node.stmts());
        return null;
    }

    public Void visit(CastNode node){
        bindType(node.typeNode());
        super.visit(node);
        return null;
    }

    public Void visit(SizeofExprNode node){
        bindType(node.typeNode());
        super.visit(node);
        return null;
    }

    public Void visit(IntegerLiteralNode node){
        bindType(node.typeNode());
        return null;
    }

    public Void visit(StringLiteralNode node){
        bindType(node.typeNode());
        return null;
    }

    @Override
    public Void visit(CommaNode node) {
        node.lhs().accept(this);
        node.rhs().accept(this);
        return null;
    }

    @Override
    public Void visit(FloatLiteralNode node) {
        bindType(node.typeNode());
        return null;
    }

    @Override
    public Void visit(InitNode initNode) {
        for(ExprNode e : initNode.getInits()){
            e.accept(this);
        }
        return null;
    }
}
