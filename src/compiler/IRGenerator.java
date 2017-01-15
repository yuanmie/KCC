package compiler;

import asm.Label;
import entity.*;
import ir.Float;
import type.Type;
import ast.*;
import ir.*;
import type.*;
import utils.ListUtils;

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

    private void checkJumpLinks(Map<String, JumpEntry> jumpMap) {
        for(Map.Entry<String, JumpEntry> ent : jumpMap.entrySet()){
            String labelName = ent.getKey();
            JumpEntry jump = ent.getValue();
            if(jump.isDefined){
                throw new Error("undefined label: " + labelName);
            }
            if(jump.numRefered == 0){
                System.out.println("useless label: " + labelName);
            }
        }
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

    private void assign(Expr lhs, Expr rhs){
        stmts.add(new Assign(addressOf(lhs), rhs));
    }

    private DefinedVariable tmpVar(type.Type t){
        return scopeStack.getLast().allocateTmp(t);
    }

    private void label(Label label){
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
        Expr e = node.expr().accept(this);
        if(e != null){
            System.out.println("warn: useless expression");
        }
        return null;
    }

    @Override
    public Void visit(IfNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();

        Expr cond = transformExpr(node.cond());
        if(node.elseBody() == null){
            cjump(cond, thenLabel, endLabel);
            label(thenLabel);
            transformStmt(node.thenBody());
            label(endLabel);
        }else{
            cjump(cond, thenLabel, elseLabel);
            label(thenLabel);
            transformStmt(node.thenBody());
            jump(endLabel);
            label(elseLabel);
            transformStmt(node.elseBody());
            label(endLabel);
        }
        return null;
    }

    @Override
    public Void visit(SwitchNode node) {
        List<Case> cases = new ArrayList<>();
        Label endLabel = new Label();
        Label defaultLabel = endLabel;

        Expr cond = transformExpr(node.cond());
        for(CaseNode c : node.cases()){
            if(c.isDefault()){
                defaultLabel = c.label();
            }else{
                for(ExprNode val : c.values()){
                    Expr v = transformExpr(val);
                    cases.add(new Case(((Int)v).value(), c.label()));
                }
            }
        }

        stmts.add(new Switch(cond, cases, defaultLabel, endLabel));
        pushBreak(endLabel);
        for(CaseNode c : node.cases()){
            label(c.label());
            transformStmt(c.body());
        }
        popBreak();
        label(endLabel);
        return null;
    }

    @Override
    public Void visit(CaseNode node) {
        throw new Error("must not happen");
    }

    @Override
    public Void visit(WhileNode node) {
        Label begLabel = new Label();
        Label bodyLabel = new Label();
        Label endLabel = new Label();

        label(begLabel);
        cjump(transformExpr(node.cond()), bodyLabel, endLabel);
        label(bodyLabel);
        pushContinue(begLabel);
        pushBreak(endLabel);
        transformStmt(node.body());
        popBreak();
        popContinue();
        jump(begLabel);
        label(endLabel);
        return null;
    }

    @Override
    public Void visit(DoWhileNode node) {
        Label begLabel = new Label();
        Label contLabel = new Label();
        Label endLabel = new Label();

        pushContinue(contLabel);
        pushBreak(endLabel);
        label(begLabel);
        transformStmt(node.body());
        popBreak();
        popContinue();
        label(contLabel);
        cjump(transformExpr(node.cond()), begLabel, endLabel);
        label(endLabel);
        return null;
    }

    @Override
    public Void visit(ForNode node) {
        Label begLabel = new Label();
        Label bodyLabel = new Label();
        Label contLabel = new Label();
        Label endLabel = new Label();

        if(node.init() != null){
            transformStmt(node.init());
        }
        label(begLabel);
        cjump(transformExpr(node.cond()), bodyLabel, endLabel);
        label(bodyLabel);
        pushContinue(contLabel);
        pushBreak(endLabel);
        transformStmt(node.body());
        popBreak();
        popContinue();
        label(contLabel);
        if(node.incr() != null){
            transformStmt(node.incr());
        }
        jump(begLabel);
        label(endLabel);
        return null;
    }

    @Override
    public Void visit(BreakNode node) {
        jump(currentBreakTarget());
        return null;
    }

    @Override
    public Void visit(ContinueNode node) {
        jump(currentContinueTarget());
        return null;
    }

    @Override
    public Void visit(GotoNode node) {
        jump(referLabel(node.target()));
        return null;
    }

    @Override
    public Void visit(LabelNode node) {
        stmts.add(new LabelStmt(defineLabel(node.name())));
        if(node.stmt() != null){
            transformStmt(node.stmt());
        }
        return null;
    }

    private Label defineLabel(String name) {
        JumpEntry ent = getJumpEntry(name);
        if(ent.isDefined){
            throw new Error("duplicated jump labels in " + name + "(): " + name);
        }
        ent.isDefined = true;
        return ent.label;
    }

    private Label referLabel(String name){
        JumpEntry ent = getJumpEntry(name);
        ent.numRefered++;
        return ent.label;
    }

    private JumpEntry getJumpEntry(String name) {
        JumpEntry ent = getJumpEntry(name);
        if(ent == null){
            ent = new JumpEntry(new Label());
            jumpMap.put(name, ent);
        }
        return ent;
    }


    @Override
    public Void visit(ReturnNode node) {
        stmts.add(new Return(node.expr() == null ? null : transformExpr(node.expr())));
        return null;
    }


    @Override
    public Expr visit(AssignNode node) {
        if(isStatement()){
            Expr rhs = transformExpr(node.rhs());
            assign(transformExpr(node.lhs()), rhs);
            return null;
        }else{
            DefinedVariable tmp = tmpVar(node.rhs().type());
            assign(ref(tmp), transformExpr(node.rhs()));
            assign(transformExpr(node.lhs()), ref(tmp));
            return ref(tmp);
        }
    }

    @Override
    public Expr visit(OpAssignNode node) {
        Expr rhs = transformExpr(node.rhs());
        Expr lhs = transformExpr(node.lhs());
        Type t = node.lhs().type();
        Op op = Op.internBinary(node.operator(), t.isSigned());
        return transformOpAssign(op, t, lhs, rhs);

    }

    @Override
    public Expr visit(CondExprNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        Expr cond = transformExpr(node.cond());
        cjump(cond, thenLabel, elseLabel);
        label(thenLabel);
        assign(ref(var), transformExpr(node.thenExpr()));
        jump(endLabel);
        label(elseLabel);
        assign(ref(var), transformExpr(node.elseExpr()));
        jump(endLabel);
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    @Override
    public Expr visit(LogicalOrNode node) {
        Label rightLabel = new Label();
        Label endlable = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(ref(var), transformExpr(node.left()));
        cjump(ref(var), rightLabel, endlable);
        label(rightLabel);
        assign(ref(var) ,transformExpr(node.right()));
        label(endlable);
        return isStatement() ? null : ref(var);
    }

    @Override
    public Expr visit(LogicalAndNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(
                ref(var), transformExpr(node.left()));
        cjump( ref(var), endLabel, rightLabel);
        label(rightLabel);
        assign(
                ref(var), transformExpr(node.right()));
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    @Override
    public Expr visit(BinaryOpNode node) {
        Expr right = transformExpr(node.right());
        Expr left = transformExpr(node.left());
        Op op = Op.internBinary(node.operator(), node.type().isSigned());
        Type t = node.type();
        Type r = node.right().type();
        Type l = node.left().type();

        if(isPointerDiff(op, l, r)){
            Expr tmp = new Bin(asmType(t), op, left, right);
            return new Bin(asmType(t), Op.S_DIV, tmp, ptrBaseSize(l));
        }else if(isPointerArithmetic(op, l)){
            return new Bin(asmType(t), op, left, new Bin(asmType(r), Op.MUL,
                    right, ptrBaseSize(l)));
        }else if(isPointerArithmetic(op, r)){
            return new Bin(asmType(t), op,  new Bin(asmType(l), Op.MUL,
                    right, ptrBaseSize(r)), left);
        }else{
            return new Bin(asmType(t), op, left, right);
        }
    }

    private boolean isPointerDiff(Op op, Type l, Type r) {
        return op == Op.SUB && l.isPointer() && r.isPointer();
    }

    @Override
    public Expr visit(UnaryOpNode node) {
        if(node.operator().equals("+")){
            return transformExpr(node.expr());
        }else{
            return new Uni(asmType(node.type()),
                    Op.internUnary(node.operator()),
                    transformExpr(node.expr()));
        }
    }

    @Override
    public Expr visit(PrefixOpNode node) {
        Type t = node.expr().type();
        return transformOpAssign(binOp(node.operator()), t, transformExpr(node.expr()),
        imm(t, 1));
    }

    private Int imm(Type t, long n) {
        if(t.isPointer()){
            return new Int(ptrdiff_t(), n);
        }else{
            return new Int(int_t(), n);
        }
    }

    private asm.Type int_t() {
        return asm.Type.get(typeTable.longSize());
    }

    private Op binOp(String uniOp){
        return uniOp.equals("++") ? Op.ADD : Op.SUB;
    }

    private Expr transformOpAssign(Op op, Type lhsType, Expr lhs, Expr rhs){
        if(lhs.isVar()){
            assign(lhs, bin(op, lhsType, lhs, rhs));
            return isStatement() ? null : lhs;
        }else{
            DefinedVariable a = tmpVar(pointerTo(lhsType));
            assign(ref(a), addressOf(lhs));
            assign(mem(a), bin(op, lhsType, mem(a), rhs));
            return isStatement() ? null : mem(a);
        }
    }

    private Expr addressOf(Expr expr) {
        if(expr.type() == asm.Type.Float32){
            return expr.addressNode(ptr_t_float());
        }
        return expr.addressNode(ptr_t());
    }

    private asm.Type ptr_t() {
        return asm.Type.get(typeTable.pointerSize());
    }

    //for find a float , set float size is pointerSize() - 1
    private asm.Type ptr_t_float(){
        return asm.Type.get((int)typeTable.pointerSize() - 1);
    }

    private Expr mem(Entity ent) {
        return new Mem(asmType(ent.type().baseType()), ref(ent));
    }

    private asm.Type asmType(Type type) {
        if(type.isVoid()){
            return int_t();
        }
        return asm.Type.get(type.size());
    }

    private Var ref(Entity ent) {
        return new Var(varType(ent.type()), ent);
    }

    private asm.Type varType(Type type) {
        if(!type.isScalar()){
            return null;
        }
        return asm.Type.get(type.size());
    }

    private Mem mem(Expr expr, Type t){
        return new Mem(asmType(t), expr);
    }

    private Type pointerTo(Type t) {
        return typeTable.pointerTo(t);
    }

    private Bin bin(Op op, Type leftType, Expr left, Expr right){
        if(isPointerArithmetic(op, leftType)){
            return new Bin(left.type(), op, left, new Bin(right.type(), Op.MUL,
                    right, ptrBaseSize(leftType)));
        }else{
            return new Bin(left.type(), op, left, right);
        }
    }

    private Expr ptrBaseSize(Type t) {
        return new Int(ptrdiff_t(), t.baseType().size());
    }

    private asm.Type ptrdiff_t() {
        return asm.Type.get((int)typeTable.longSize());
    }

    private boolean isPointerArithmetic(Op op, Type operandType) {
        switch(op){
            case ADD:
            case SUB:
                return operandType.isPointer();
            default:
                return false;
        }
    }

    @Override
    public Expr visit(SuffixOpNode node) {
        Expr expr = transformExpr(node.expr());
        Type t = node.expr().type();
        Op op = binOp(node.operator());

        if(isStatement()){
            transformOpAssign(op, t, expr, imm(t, 1));
            return null;
        }else if(expr.isVar()){
            DefinedVariable v = tmpVar(t);
            assign(ref(v), expr);
            assign(expr, bin(op, t, ref(v), imm(t, 1)));
            return ref(v);
        }else{
            DefinedVariable a = tmpVar(pointerTo(t));
            DefinedVariable v = tmpVar(t);
            assign(ref(a), addressOf(expr));
            assign(ref(v), mem(a));
            assign(mem(a), bin(op, t, mem(a), imm(t, 1)));
            return ref(v);
        }
    }

    @Override
    public Expr visit(ArefNode node) {
        Expr expr = transformExpr(node.baseExpr());
        Expr offset = new Bin(ptrdiff_t(), Op.MUL,
                size(node.elementSize()), transformIndex(node));
        Bin addr = new Bin(ptr_t(), Op.ADD, expr, offset);
        return mem(addr, node.type());
    }

    private Expr size(long l) {
        return new Int(size_t(), l);
    }

    private asm.Type size_t() {
        return asm.Type.get(typeTable.longSize());
    }

    private Expr transformIndex(ArefNode node) {
        if(node.isMultiDimension()){
            return new Bin(int_t(), Op.ADD, transformExpr(node.index()),
                    new Bin(int_t(), Op.MUL, new Int(int_t(), node.length()),
                            transformIndex((ArefNode)node.expr())));
        }else{
            return transformExpr(node.index());
        }
    }

    @Override
    public Expr visit(MemberNode node) {
        Expr expr = addressOf(transformExpr(node.expr()));
        Expr offset = ptrdiff(node.offset());
        Expr addr = new Bin(ptr_t(), Op.ADD, expr, offset);
        return node.isLoadable() ? mem(addr, node.type()) : addr;
    }

    private Expr ptrdiff(long offset) {
        return new Int(ptrdiff_t(), offset);
    }

    @Override
    public Expr visit(PtrMemberNode node) {
        Expr expr = transformExpr(node.expr());
        Expr offset = ptrdiff(node.offset());
        Expr addr = new Bin(ptr_t(), Op.ADD, expr, offset);
        return node.isLoadable() ? mem(addr, node.type()) : addr;

    }

    @Override
    public Expr visit(FuncallNode node) {
        List<Expr> args = new ArrayList<>();
        for(ExprNode arg : ListUtils.reverse(node.args())){
            args.add(0, transformExpr(arg));
        }
        Expr call = new Call(asmType(node.type()),
                transformExpr(node.expr()), args);
        if(isStatement()){
            stmts.add(new ExprStmt(call));
            return null;
        }else{
            DefinedVariable tmp = tmpVar(node.type());
            assign(ref(tmp), call);
            return ref(tmp);
        }
    }

    @Override
    public Expr visit(DereferenceNode node) {
        Expr addr = transformExpr(node.expr());
        return node.isLoadable() ? mem(addr, node.type()) : addr;
    }

    @Override
    public Expr visit(AddressNode node) {
        Expr e = transformExpr(node.expr());
        return node.expr().isLoadable() ? addressOf(e) : e;
    }

    @Override
    public Expr visit(CastNode node) {
        if(node.isEffectiveCast()){
            return new Uni(asmType(node.type()),
                    node.expr().type().isSigned() ? Op.S_CAST : Op.U_CAST,
                    transformExpr(node.expr()));
        }else if(isStatement()){
            transformStmt(node.expr());
            return null;
        }else{
            return transformExpr(node.expr());
        }

    }

    @Override
    public Expr visit(SizeofExprNode node) {
        return new Int(size_t(), node.expr().allocSize());
    }

    @Override
    public Expr visit(SizeofTypeNode node) {
        return new Int(size_t(), node.operand().allocSize());
    }

    @Override
    public Expr visit(VariableNode node) {
        if(node.entity().isConstant()){
            return transformExpr(node.entity().value());
        }
        Var var = ref(node.entity());
        return node.isLoadable() ? var : addressOf(var);
    }

    @Override
    public Expr visit(IntegerLiteralNode node) {
        return new Int(asmType(node.type()), node.value());
    }

    @Override
    public Expr visit(StringLiteralNode node) {
        return new Str(asmType(node.type()), node.entry());
    }

    @Override
    public Expr visit(CommaNode node) {
        node.lhs().accept(this);
        return node.rhs().accept(this);
    }

    @Override
    public Expr visit(FloatLiteralNode node) {
        return new Float(asmType(node.type()), node.entry());
    }

    @Override
    public Expr visit(InitNode initNode) {
        return null;
    }


}
