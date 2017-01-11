package compiler;

import asm.Label;
import entity.DefinedFunction;
import entity.DefinedVariable;
import entity.Jump;
import entity.LocalScope;
import type.Type;
import ast.*;
import ir.*;
import type.*;

import java.util.*;

public class IRGenerator implements ASTVisitor<Void, Expr>{

    private final TypeTable typeTable;

    public IRGenerator(TypeTable types) {
        this.typeTable = types;
    }

    public IR generate(AST ast) {
        for(DefinedVariable var : ast.definedVariables()){
            if(var.hasInitializer()){
                var.setIR(transformExpr(var.initializer()));
            }
        }

        for(DefinedFunction f : ast.definedFunctions()){
            f.setIR(compileFunctionBody(f));
        }

        return ast.ir();
    }

    List<Stmt> stmts;
    LinkedList<LocalScope> scopeStack;
    LinkedList<Label> breakStack;
    LinkedList<Label> continueStack;
    Map<String, JumpEntry> jumpMap;

    class JumpEntry{
        public Label label;
        public long numRefered;
        public boolean isDefined;

        public JumpEntry(Label lable){
            this.label = lable;
            numRefered = 0;
            isDefined = false;
        }
    }
    private List<Stmt> compileFunctionBody(DefinedFunction f) {
        stmts = new ArrayList<>();
        scopeStack = new LinkedList<>();
        breakStack = new LinkedList<>();
        continueStack = new LinkedList<>();
        jumpMap = new HashMap<>();
        transformStmt(f.body());
        checkJumpLinks(jumpMap);
        return stmts;
    }

    private void transformStmt(StmtNode node){
        node.accept(this);
    }

    private void transformStmt(ExprNode node){
        node.accept(this);
    }

    private int exprNestLevel = 0;

    private Expr transformExpr(ExprNode node){
        exprNestLevel++;
        Expr e = node.accept(this);
        exprNestLevel--;
        return e;
    }

    private boolean isStatement(){
        return (exprNestLevel == 0);
    }

    private void assign(Expr lsh, Expr rhs){
        stmts.add(new Assign(addressOf(lhs), rhs));
    }

    private DefinedVariable tmpVar(type.Type t){
        return scopeStack.getLast().allocateTmp(t);
    }

    private void lable(Label label){
        stmts.add(new LabelStmt(label));
    }

    private void jump(Label target){
        stmts.add(new Jump(target));
    }

    private void cjump(Expr cond, Label thenLabel, Label elseLabel){
        stmts.add(new CJump(cond, thenLabel, elseLabel));
    }

    private void pushBreak(Label label){
        breakStack.add(label);
    }

    private void popBreak(){
        if(breakStack.isEmpty()){
            throw new Error("unmatched push/pop for break stack");
        }
        breakStack.removeLast();
    }

    private Label currentBreakTarget(){
        if(breakStack.isEmpty()){
            throw new Error("break from out of loop");
        }
        return breakStack.getLast();
    }

    private void pushContinue(Label label){
        continueStack.add(label);
    }

    private void popContinue(){
        if(continueStack.isEmpty()){
            throw new Error("unmatched push/pop for continue stack");
        }
        continueStack.removeLast();
    }

    private Label currentContinueTarget(){
        if(breakStack.isEmpty()){
            throw new Error("continue from out of loop");
        }
        return continueStack.getLast();
    }


    @Override
    public Void visit(BlockNode node) {
        scopeStack.add(node.scope());
        for(DefinedVariable var : node.variables()){
            if(var.hasInitializer()){
                if(var.isPrivate()){
                    var.setIR(transformExpr(var.initializer()));
                }else{
                    //array, struct ,union init
                    if(var.initializer() instanceof InitNode){
                        Type type = var.type();
                        CompositeTypeinit(var, type, (InitNode)var.initializer(), null, null);
                    }else if(var.initializer()  instanceof VariableNode
                            && ((VariableNode)var.initializer()).type() instanceof CompositeType){
                        CompositeTypeAssign(var, (VariableNode)var.initializer());
                    }else{
                        assign(ref(var), transformExpr(var.initializer()));
                    }
                }
            }
        }
        for(StmtNode s : node.stmts()){
            transformStmt(s);
        }
        scopeStack.removeLast();
        return null;
    }

    private void CompositeTypeAssign(DefinedVariable src, VariableNode dest) {
        Type dtype = dest.type();
        Type stype = src.type();
        MemberNode s, d;
        if(dtype instanceof StructType){

            List<Slot> dmembers = ((StructType) dtype).members(); //dest members
            List<Slot> smembers = ((StructType) dtype).members(); //src members
            Slot slot1, slot2;
            int membersize = dmembers.size();
            for(int i = 0; i < membersize; i++){
                slot1 = dmembers.get(i);
                slot2 = smembers.get(i);
                d = new MemberNode(dest, dmembers.get(i).name());
                s = new MemberNode(new VariableNode(src), smembers.get(i).name());
                if(slot1.type() instanceof StructType){
                    CompositeStructTypeAssign(s, d, (StructType)slot1.type(), (StructType)slot2.type(), null, null);
                }else if(slot1.type() instanceof ArrayType){
                    CompositeArrayTypeAssign(s, d, (ArrayType)slot1.type(), (ArrayType)slot2.type(), null, null);
                }else if(slot1.type() instanceof UnionType){
                    CompositeUnionTypeAssign(s, d, (UnionType)slot1.type(), (UnionType)slot2.type(), null, null);
                }else{
                    assign(
                            transformExpr(s),
                            transformExpr(d));
                }

            }
        }
    }

    private void CompositeStructTypeAssign(MemberNode s, MemberNode d,
                                           StructType type1, StructType type2,
                                           ArefNode arefs, ArefNode arefd) {
        List<Slot> smembers = type1.members(); //dest members
        List<Slot> dmembers = type2.members(); //src members
        Slot slot1, slot2;
        MemberNode sm, dm;
        int membersize = dmembers.size();
        for(int i = 0; i < membersize; i++){
            slot1 = smembers.get(i);
            slot2 = dmembers.get(i);
            if(arefs != null && arefd != null){
                dm = new MemberNode(arefd, dmembers.get(i).name());
                sm = new MemberNode(arefs, smembers.get(i).name());
            }else{
                dm = new MemberNode(d, dmembers.get(i).name());
                sm = new MemberNode(s, smembers.get(i).name());
            }

            if(slot1.type() instanceof StructType){
                CompositeStructTypeAssign(sm, dm, (StructType)slot1.type(), (StructType)slot1.type(), null, null);
            }else if(slot1.type() instanceof ArrayType){
                CompositeArrayTypeAssign(sm, dm, (ArrayType)slot1.type(), (ArrayType)slot2.type(), null, null);
            }else if(slot1.type() instanceof UnionType){
                CompositeUnionTypeAssign(sm, dm, (UnionType)slot1.type(), (UnionType)slot2.type(), null, null);
            }else{
                assign( transformExpr(sm),
                        transformExpr(dm));
            }
        }
    }


    private void CompositeArrayTypeAssign(MemberNode s, MemberNode d, ArrayType type, ArrayType type1, ArefNode arefs, ArefNode arefd) {
        long length = ((ArrayType) type1).length();
        ArefNode aref1, aref2;
        for(int i = 0; i < length; i++){
            if(arefs != null && arefd != null){
                aref1 = new ArefNode(arefs, new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
                aref2 = new ArefNode(arefd, new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
            }else{
                aref1 = new ArefNode(s, new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
                aref2 = new ArefNode(d, new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
            }

            if(type.baseType() instanceof StructType){
                CompositeStructTypeAssign(null, null, (StructType)type.baseType(), (StructType)type.baseType(), aref1, aref2);
            }else if(type.baseType() instanceof  UnionType){

            }else if(type.baseType() instanceof  ArrayType){
                CompositeArrayTypeAssign(null, null, (ArrayType)type.baseType(), (ArrayType)type.baseType(), aref1, aref2);
            }else{
                assign(
                        transformExpr(aref1),
                        transformExpr(aref2));
            }
        }
    }

    private void CompositeUnionTypeAssign(MemberNode s, MemberNode d, UnionType type1, UnionType type2, ArefNode arefs, ArefNode arefd) {

        Slot slots = type1.members().get(0);
        Slot slotd = type2.members().get(0);
        MemberNode sm, dm;


        if(arefs != null && arefd != null){
            dm = new MemberNode(arefd, slots.name());
            sm = new MemberNode(arefs, slotd.name());
        }else{
            dm = new MemberNode(d, slotd.name());
            sm = new MemberNode(s, slots.name());
        }

        if(slots.type() instanceof StructType){
            CompositeStructTypeAssign(s, d, (StructType)slots.type(), (StructType)slotd.type(), null, null);
        }else if(slots.type() instanceof  UnionType){
            CompositeUnionTypeAssign(s, d, (UnionType)slots.type(), (UnionType)slotd.type(), null, null);
        }else if(slots.type() instanceof  ArrayType){
            CompositeArrayTypeAssign(s, d, (ArrayType)slots.type(), (ArrayType)slotd.type(), null, null);
        }else{
            assign(
                    transformExpr(sm),
                    transformExpr(dm));
        }
    }

    public void CompositeTypeinit(DefinedVariable var, Type type, InitNode inits, ArefNode aref, MemberNode m){
        int i = 0;
        //int size is 4;
        ArefNode arefnode = null;
        CompositeType ctype = null;
        MemberNode mem = null;
        if(type instanceof CompositeType){
            ctype = (CompositeType) type;
        }

        //
        for(ExprNode e : inits.getInits()){
            if(type instanceof ArrayType){
                if(aref == null && m == null){
                    arefnode =  new ArefNode(new VariableNode(var), new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
                }else if(m == null){
                    arefnode = new ArefNode(aref, new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
                }else{
                    arefnode = new ArefNode(m, new IntegerLiteralNode(IntegerTypeRef.intRef(), type, i));
                }
                if(e instanceof InitNode){
                    CompositeTypeinit(var, type.baseType(), (InitNode)e, arefnode, null);
                }else{
                    assign(
                            transformExpr(arefnode),
                            transformExpr(e));
                }
            }else if(type instanceof CompositeType){
                if(aref == null && m == null) {
                    mem = new MemberNode(new VariableNode(var), ctype.members().get(i).name());
                }else if(m == null){
                    mem = new MemberNode(aref, ctype.members().get(i).name());
                }else{
                    mem = new MemberNode(m, ctype.members().get(i).name());
                }

                if(e instanceof InitNode){
                    CompositeTypeinit(var, ctype.members().get(i).type(),(InitNode)e, null, mem);
                }else{
                    assign( transformExpr(mem),
                            transformExpr(e));
                }
            }
            ++i;
        }
    }
    @Override
    public Void visit(ExprStmtNode node) {
        return null;
    }

    @Override
    public Void visit(IfNode node) {
        return null;
    }

    @Override
    public Void visit(SwitchNode node) {
        return null;
    }

    @Override
    public Void visit(CaseNode node) {
        return null;
    }

    @Override
    public Void visit(WhileNode node) {
        return null;
    }

    @Override
    public Void visit(DoWhileNode node) {
        return null;
    }

    @Override
    public Void visit(ForNode node) {
        return null;
    }

    @Override
    public Void visit(BreakNode node) {
        return null;
    }

    @Override
    public Void visit(ContinueNode node) {
        return null;
    }

    @Override
    public Void visit(GotoNode node) {
        return null;
    }

    @Override
    public Void visit(LabelNode node) {
        return null;
    }

    @Override
    public Void visit(ReturnNode node) {
        return null;
    }

    @Override
    public Expr visit(AssignNode node) {
        return null;
    }

    @Override
    public Expr visit(OpAssignNode node) {
        return null;
    }

    @Override
    public Expr visit(CondExprNode node) {
        return null;
    }

    @Override
    public Expr visit(LogicalOrNode node) {
        return null;
    }

    @Override
    public Expr visit(LogicalAndNode node) {
        return null;
    }

    @Override
    public Expr visit(BinaryOpNode node) {
        return null;
    }

    @Override
    public Expr visit(UnaryOpNode node) {
        return null;
    }

    @Override
    public Expr visit(PrefixOpNode node) {
        return null;
    }

    @Override
    public Expr visit(SuffixOpNode node) {
        return null;
    }

    @Override
    public Expr visit(ArefNode node) {
        return null;
    }

    @Override
    public Expr visit(MemberNode node) {
        return null;
    }

    @Override
    public Expr visit(PtrMemberNode node) {
        return null;
    }

    @Override
    public Expr visit(FuncallNode node) {
        return null;
    }

    @Override
    public Expr visit(DereferenceNode node) {
        return null;
    }

    @Override
    public Expr visit(AddressNode node) {
        return null;
    }

    @Override
    public Expr visit(CastNode node) {
        return null;
    }

    @Override
    public Expr visit(SizeofExprNode node) {
        return null;
    }

    @Override
    public Expr visit(SizeofTypeNode node) {
        return null;
    }

    @Override
    public Expr visit(VariableNode node) {
        return null;
    }

    @Override
    public Expr visit(IntegerLiteralNode node) {
        return null;
    }

    @Override
    public Expr visit(StringLiteralNode node) {
        return null;
    }

    @Override
    public Expr visit(CommaNode commaNode) {
        return null;
    }

    @Override
    public Expr visit(FloatLiteralNode floatLiteralNode) {
        return null;
    }

    @Override
    public Expr visit(InitNode initNode) {
        return null;
    }


}
