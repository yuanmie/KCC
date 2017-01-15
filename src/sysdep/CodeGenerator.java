package sysdep;

import asm.*;
import entity.*;
import ir.*;
import ir.Float;
import utils.AsmUtils;
import utils.ListUtils;

import java.util.ArrayList;
import java.util.List;


public class CodeGenerator implements IRVisitor<Void, Void>, ELFConstants{
    final Type naturalType;
    static final String LABEL_SYMBOL_BASE = ".L";
    static final String CONST_SYMBOL_BASE = ".LC";

    public CodeGenerator(Type naturalType){
        this.naturalType = naturalType;
    }

    public AssemblyCode generate(IR ir){
        locateSymbols(ir);
        return generateAssemblyCode(ir);
    }

    private AssemblyCode generateAssemblyCode(IR ir) {
        AssemblyCode file = newAssemblyCode();
        file._file(ir.fileName());
        if(ir.isGlobalVariableDefined()){
            generateDataSection(file, ir.definedGlobalVariables());
        }

        if(ir.isStringLiteralDefined()){
            generateReadOnlyDataSection(file, ir.constantTable());
        }

        if(ir.isFunctionDefined()){
            generateTextSection(file, ir.definedFunctions());
        }

        if(ir.isCommonSymbolDefined()){
            generateCommonSymbols(file, ir.definedCommonSymbols());
        }
        return file;
    }

    private void generateCommonSymbols(AssemblyCode file, List<DefinedVariable> vars) {
        for(DefinedVariable var : vars){
            Symbol sym = globalSymbol(var.symbolString());
            if(var.isPrivate()){
                file._local(sym);
            }
            file._comm(sym, var.allocSize(), var.alignment());
        }
    }

    private void generateTextSection(AssemblyCode file, List<DefinedFunction> functions) {
        file._text();
        for(DefinedFunction func : functions){
            Symbol sym = globalSymbol(func.name());
            if(!func.isPrivate()){
                file._globl(sym);
            }
            file._type(sym, "@function");
            file.label(sym);
            compileFunctionBody(file, func);
            file._size(sym, ".-" + sym.toSource());
        }
    }

    private void compileFunctionBody(AssemblyCode file, DefinedFunction func) {
        StackFrameInfo frame = new StackFrameInfo();

        locateParameters(func.parameters());
        frame.lvarSize = locateLocalVariables(func.lvarScope());
        AssemblyCode body = compileStmt(func);
        frame.saveRegs = usedCalleeSaveRegisters(body);
        frame.tempSize = body.virtualStack.maxSize();

        fixLocalVariableOffsets(func.lvarScope(), frame.lvarOffset());
        fixTempVariableOffsets(body, frame.tempOffset());

        generateFunctionBody(file, body, frame);
    }

    private void generateFunctionBody(AssemblyCode file, AssemblyCode body, StackFrameInfo frame) {
        file.virtualStack.reset();
        prologue(file, frame.saveRegs, frame.frameSize());

        file.addAll(body.assemblies());
        epilogue(file, frame.saveRegs);
        file.virtualStack.fixOffset(0);
    }

    private void epilogue(AssemblyCode file, List<Register> saveRegs) {
        for(Register reg : ListUtils.reverse(saveRegs)){
            file.virtualPop(reg);
        }

        file.mov(bp(),sp());
        file.pop(bp());
        file.ret();
    }

    private void prologue(AssemblyCode file, List<Register> saveRegs, long frameSize) {
        file.push(bp());
        file.mov(sp(), bp());
        for(Register reg : saveRegs){
            file.virtualPush(reg);
        }
        extendStack(file, frameSize);
    }

    private void extendStack(AssemblyCode file, long len) {
        if(len > 0){
            file.sub(imm(len), sp());
        }
    }


    private void fixTempVariableOffsets(AssemblyCode body, long len) {
        body.virtualStack.fixOffset(-len);
    }

    private void fixLocalVariableOffsets(LocalScope scope, long len) {
        for(DefinedVariable var : scope.allLocalVariables()){
            var.memref().fixOffset(-len);
        }
    }

    private List<Register> usedCalleeSaveRegisters(AssemblyCode body) {
        List<Register> result = new ArrayList<>();
        for(Register reg : calleeSaveRegisters()){
                result.add(reg);
        }
        result.remove(bp());
        return result;

    }

    static final RegisterClass[] CALLEE_SAVE_REGISTERS = {
            RegisterClass.BX, RegisterClass.BP,
            RegisterClass.SI, RegisterClass.DI
    };

    private List<Register> calledSaveRegistersCache = null;

    private List<Register> calleeSaveRegisters() {
        if(calledSaveRegistersCache == null){
            List<Register> regs = new ArrayList<>();
            for(RegisterClass c : CALLEE_SAVE_REGISTERS){
                regs.add(new Register(c, naturalType));
            }
            calledSaveRegistersCache = regs;
        }
        return calledSaveRegistersCache;
    }

    private AssemblyCode as;
    private Label epilogue;
    private AssemblyCode compileStmt(DefinedFunction func) {
        as = newAssemblyCode();
        epilogue = new Label();
        for(Stmt s : func.ir()){
            compileStmt(s);
        }
        as.label(epilogue);
        return as;
    }

    private void compileStmt(Stmt stmt) {
        stmt.accept(this);
    }

    private long locateLocalVariables(LocalScope scope) {
        return locateLocalVariables(scope, 0);
    }

    private long locateLocalVariables(LocalScope scope, long parentStackLen) {
        long len = parentStackLen;
        for(DefinedVariable var : scope.localVariables()){
            len = alignStack(len + var.allocSize());
            var.setMemref(relocatableMem(-len, bp()));
        }

        long maxLen = len;
        for(LocalScope s : scope.children()){
            long childLen = locateLocalVariables(s, len);
            maxLen = Math.max(maxLen, childLen);
        }

        return maxLen;
    }

    private MemoryReference relocatableMem(long offset, Register base) {
        return IndirectMemoryReference.relocatable(offset, base);
    }

    private long alignStack(long size) {
        return AsmUtils.align(size, STACK_WORD_SIZE);
    }

    static final private long PARAM_START_WORD = 2;

    private void locateParameters(List<CBCParameter> parameters) {
        long numWords = PARAM_START_WORD;
        for(CBCParameter var : parameters){
            var.setMemref(mem(stackSizeFromWordNum(numWords), bp()));
            numWords++;
        }
    }

    private IndirectMemoryReference mem(long offset, Register reg) {
        return new IndirectMemoryReference(offset, reg);
    }

    private IndirectMemoryReference mem(Register reg){
        return new IndirectMemoryReference(0, reg);
    }

    private IndirectMemoryReference mem(Symbol offset, Register reg){
        return new IndirectMemoryReference(offset, reg);
    }

    private ImmediateValue imm(long n){
        return new ImmediateValue(n);
    }

    private ImmediateValue imm(Literal lit){
        return new ImmediateValue(lit);
    }

    private long stackSizeFromWordNum(long numWords) {
        return numWords * STACK_WORD_SIZE;
    }


    private void generateReadOnlyDataSection(AssemblyCode file, ConstantTable constants) {
        file._section(".rodata");
        for(ConstantEntry ent : constants){
            file.label(ent.symbol());
            if(ent.floatValue() != null){
                file._float(ent.floatValue());
            }else{
                file._string(ent.value());
            }
        }
    }

    private void generateDataSection(AssemblyCode file, List<DefinedVariable> gvars) {
        file._data();
        for(DefinedVariable var : gvars){
            Symbol sym = globalSymbol(var.symbolString());
            if(!var.isPrivate()){
                file._globl(sym);
            }
            file._align(var.alignment());
            file._type(sym, "@object");
            file._size(sym, var.allocSize());
            file.label(sym);
            generatorImmediate(file, var.type().allocSize(), var.ir());
        }
    }

    private void generatorImmediate(AssemblyCode file, long size, Expr node) {
        if(node instanceof Int){
            Int expr  = (Int)node;
            switch ((int)size){
                case 1: file._byte(expr.value()); break;
                case 2: file._value(expr.value()); break;
                case 3: file._value(expr.value()); break;
                case 4:file._quad(expr.value()); break;
                    default:
                        throw new Error("entry size must be 1,2,4,8");
            }
        }else if(node instanceof Str){
            Str expr = (Str)node;
            switch ((int)size){
                case 4:file._long(expr.symbol()); break;
                case 8:file._quad(expr.symbol()); break;
                default:
                    throw new Error("pointer size must be 4, 8");
            }
        }else{
            throw new Error("unknown literal node type" + node.getClass());
        }
    }

    static final private long STACK_WORD_SIZE = 4;
    private AssemblyCode newAssemblyCode() {
        return new AssemblyCode(naturalType, STACK_WORD_SIZE,
                new SymbolTable(LABEL_SYMBOL_BASE), false);
    }

    private void locateSymbols(IR ir) {
        SymbolTable constSymbols = new SymbolTable(CONST_SYMBOL_BASE);
        for(ConstantEntry ent : ir.constantTable().entries()){
            locateStringLiteral(ent, constSymbols);
        }
        for(Variable var : ir.allGlobalVariables()){
            locateGlobalVariable(var);
        }

        for(Function func : ir.allFunctions()){
            locateFunction(func);
        }
    }

    private void locateFunction(Function func) {
        func.setCallingSymbol(callingSymbol(func));
        locateGlobalVariable(func);
    }

    private Symbol callingSymbol(Function func) {
        if(func.isPrivate()){
            return privateSymbol(func.symbolString());
        }else{
            Symbol sym = globalSymbol(func.symbolString());
            return sym;
        }
    }

    private void locateGlobalVariable(Entity ent) {
        Symbol sym = symbol(ent.symbolString(), ent.isPrivate());
        ent.setMemref(mem(sym));
        ent.setAddress(imm(sym));
    }

    private Symbol symbol(String s, boolean isPrivate) {
        return isPrivate ? privateSymbol(s) : globalSymbol(s);
    }

    private Symbol globalSymbol(String s) {
        return new NamedSymbol(s);
    }

    private Symbol privateSymbol(String s) {
        return new NamedSymbol(s);
    }

    private void locateStringLiteral(ConstantEntry ent, SymbolTable syms) {
        ent.setSymbol(syms.newSymbol());
        ent.setMemref(mem(ent.symbol()));
        ent.setAddress(imm(ent.symbol()));
    }

    private ImmediateValue imm(Symbol symbol) {
        return new ImmediateValue(symbol);
    }

    private MemoryReference mem(Symbol symbol) {
        return new DirectMemoryReference(symbol);
    }

    public Void visit(Call node){
        for(Expr arg : ListUtils.reverse(node.args())){
            compile(arg);

            if(arg.type() == Type.Float32){
                //float should double promote when variadic paramter, for example: printf
                if(node.isVariadic()){
                    as.pf2va();
                }else{
                    as.pushf(8);
                    as.virtualPop(ax());
                }
            }else{
                as.push(ax());
            }
        }
        if(node.isStaticCall()){
            as.call(node.function().callingSymbol());
        }else{
            compile(node.expr());
            as.callAbsolute(ax());
        }

        rewindStack(as, stackSizeFromWordNum(node.numArgs()));
        return null;
    }

    private void rewindStack(AssemblyCode file, long len) {
        if(len > 0){
            file.add(imm(len), sp());
        }

    }

    private void compile(Expr n) {
        n.accept(this);
    }

    @Override
    public Void visit(Addr node) {
        loadAddress(node.entity(), ax());
        return null;
    }

    @Override
    public Void visit(Assign node) {
        if(node.lhs().isAddr() && node.lhs().memref() != null){
            compile(node.rhs());

            if(node.lhs().type() == Type.Float32){
                storeFloat(node.lhs().memref());
            }else{
                store(ax(node.lhs().type()), node.lhs().memref());
            }
        }else if(node.rhs().isConstant()){
            compile(node.lhs());
            as.mov(ax(), cx());
            loadConstant(node.rhs(), ax());
            store(ax(node.lhs().type()), mem(cx()));
        }else{
            if(node.lhs().type() == Type.Float32){
                compile(node.rhs());
                as.pushf();
                compile(node.lhs());
                as.mov(ax(), cx());
                as.virtualPop(ax());
                store(ax(node.lhs().type()), mem(cx()));
            }else{
                compile(node.rhs());
                as.virtualPush(ax());
                compile(node.lhs());
                as.mov(ax(), cx());
                as.virtualPop(ax());
                store(ax(node.lhs().type()), mem(cx()));
            }
        }
        return null;
    }

    private void storeFloat(MemoryReference mem) {
        as.fstps(mem);
    }

    private void store(Register reg, MemoryReference mem) {
        as.mov(reg, mem);
    }

    @Override
    public Void visit(Bin node) {
        Op op = node.op();
        Type t = node.type();

        if(node.right().isConstant() && !doesRequireRegisterOperand(op)){
            if(t == Type.Float32){
                compile(node.left());
                loadConstant(node.right(), cx());
                compileFloatBinaryOp(op, ax(t), node.right().asmValue());
            }else{
                compile(node.left());
                compileBinaryOp(op, ax(t), node.right().asmValue());
            }
        }else if(node.right().isConstant()){
            compile(node.left());
            loadConstant(node.right(), cx());
            compileBinaryOp(op, ax(t), cx(t));
        }else if(node.right().isVar()){
            if(node.type() == Type.Float32){
                compile(node.left());
                loadVariable((Var)node.right(), null);
                compileFloatBinaryOp(op, ax(t), null);
            }else{
                compile(node.left());
                loadVariable((Var)node.right(), cx(t));
                compileBinaryOp(op, ax(t), cx(t));
            }
        }else if(node.right().isAddr()){
            compile(node.left());
            loadAddress(node.right().getEntityForce(), cx(t));
            compileBinaryOp(op, ax(t), cx(t));
        }else if(node.left().isConstant()
                || node.left().isVar()
                || node.left().isAddr()){

            if(node.type() == Type.Float32){
                compile(node.left());
                compile(node.right());
                compileFloatBinaryOp(op, ax(t), cx(t));
                return null;
            }else{
                compile(node.right());
                as.mov(ax(), cx());
                compile(node.left());
                compileBinaryOp(op, ax(t), cx(t));
            }

        }else{
            if(node.type() == Type.Float32){
                compile(node.right());
                as.pushf();
                compile(node.left());
                as.virtualPopFloat();
                compileFloatBinaryOp(op, ax(t), cx(t));
            }else{
                compile(node.right());
                as.virtualPush(ax());
                compile(node.left());
                as.virtualPop(cx());
                compileBinaryOp(op, ax(t), cx(t));
            }
        }
        return null;
    }

    private void loadAddress(Entity var, Register reg) {
        if(var.address() != null){
            as.mov(var.address(), reg);
        }else{
            as.lea(var.memref(), reg);
        }
    }

    private void loadVariable(Var var, Register dest) {
        if(var.memref() == null){
            Register a = dest.forType(naturalType);
            as.mov(var.address(), a);
            load(mem(a), dest.forType(var.type()));
        }else{
            if(!var.isArg() && var.type() == Type.Float32){
                loadFloatVariable(var, null);
            }else{
                load(var.memref(), dest.forType(var.type()));
            }
        }
    }

    private void loadFloat(MemoryReference mem, Register reg) {
        as.flds(mem, reg);
    }

    private void loadFloatVariable(Var var, Register dest){
        if(var.memref() == null){
            Register a = dest.forType(naturalType);
            as.mov(var.address(), a);
            load(mem(a), dest.forType(var.type()));
        }else{
            loadFloat(var.memref(), null);
        }
    }
    private void load(MemoryReference mem, Register reg) {
        as.mov(mem, reg);
    }

    private void compileBinaryOp(Op op, Register left, Operand right) {
        switch (op){
            case ADD:
                as.add(right, left);
                break;
            case SUB:
                as.add(right, left);
            case MUL:
                as.imul(right, left);
            case S_DIV:
            case S_MOD:
                as.cltd();
                as.idiv(cx(left.type));
                if(op == Op.S_MOD){
                    as.mov(dx(), left);
                }
                break;
            case U_DIV:
            case U_MOD:
                as.mov(imm(0), dx());
                as.div(cx(left.type));
                if(op == Op.U_MOD){
                    as.mov(dx(), left);
                }
                break;
            case BIT_AND:
                as.and(right, left);
                break;
            case BIT_OR:
                as.or(right, left);
                break;
            case BIT_XOR:
                as.or(right, left);
                break;
            case BIT_LSHIFT:
                as.sal(cl(), left);
                break;
            case BIT_RSHIFT:
                as.shr(cl(), left);
            case ARITH_RSHIFT:
                as.sar(cl(), left);
                break;
            default:
                as.cmp(right, ax(left.type));
                switch (op){
                    case EQ:
                        as.sete(al());
                        break;
                    case NEQ:
                        as.setne(al());
                        break;
                    case S_GT:
                        as.setg(al());
                        break;
                    case S_GTEQ:
                        as.setge(al());
                        break;
                    case S_LT:
                        as.setl(al());
                        break;
                    case S_LTEQ:
                        as.setle(al());
                    case U_GT:
                        as.seta(al());
                        break;
                    case U_GTEQ:
                        as.setae(al());
                        break;
                    case U_LT:
                        as.setb(al());
                        break;
                    case U_LTEQ:
                        as.setbe(al());
                        break;
                    default:
                        throw new Error("unknown binary operator: " + op);
                }
                as.movzx(al(), left);
        }
    }

    private void compileFloatBinaryOp(Op op, Register left, Operand right) {
        // #@@range/compileBinaryOp_arithops{
        switch (op) {
            case ADD:
                as.addFloat(right, left);
                break;
            case SUB:
                as.subFloat(right, left);
                break;
            // #@@range/compileBinaryOp_begin}
            case MUL:
                as.imulFloat(right, left);
                break;
            // #@@range/compileBinaryOp_sdiv{
            case S_DIV:
            case U_DIV:
                as.idivFloat(right, left);
                // #@@}
                break;
            default:
                // Comparison operators
                as.cmpFloat();

        }
    }
    private void loadConstant(Expr node, Register reg) {
        if(node.asmValue() != null){
            if(node.type() == Type.Float32){
                ImmediateValue imm = node.asmValue();
                imm.setIsFloat(true);
                as.flds(node.asmValue());
            }else{
                as.mov(node.asmValue(), reg);
            }
        }else if(node.memref() != null){
            as.lea(node.memref(), reg);
        }else{
            throw new Error("must not happen: constant has no asm value");
        }
    }


    private boolean doesRequireRegisterOperand(Op op) {
        switch (op){
            case S_DIV:
            case U_DIV:
            case S_MOD:
            case U_MOD:
            case BIT_LSHIFT:
            case BIT_RSHIFT:
            case ARITH_RSHIFT:
                return true;
            default:
                return false;
        }
    }


    @Override
    public Void visit(CJump node) {
        Type t = node.cond().type();
        if(t == Type.Float32) {
            compile(node.cond());
            Bin bin = (Bin) node.cond();
            Op op = bin.op();
            switch (op) {
                case EQ:
                    as.sete(al());
                    break;
                case NEQ:
                    as.setne(al());
                    break;
                case S_GT:
                    as.setg(al());
                    break;
                case S_GTEQ:
                    as.setge(al());
                    break;
                case S_LT:
                    as.setl(al());
                    break;
                case S_LTEQ:
                    as.setle(al());
                    break;
                case U_GT:
                    as.ja(node.thenLabel());
                    break;
                case U_GTEQ:
                    as.jge(node.thenLabel());
                    break;
                case U_LT:
                    as.jb(node.thenLabel());
                    break;
                case U_LTEQ:
                    as.jbe(node.thenLabel());
                    break;
                default:
                    throw new Error("unknown binary operator: " + op);
            }
            as.jmp(node.elseLabel());
            return null;
        }else{
            compile(node.cond());
            as.test(ax(t), ax(t));
            as.jnz(node.thenLabel());
            as.jmp(node.elseLabel());
            return null;
        }

    }

    @Override
    public Void visit(ExprStmt node) {
        compile(node.expr());
        return null;
    }

    @Override
    public Void visit(Int node) {
        as.mov(imm(node.value()), ax());
        return null;
    }

    @Override
    public Void visit(Float node) {
        loadConstant(node, ax());
        return null;
    }

    @Override
    public Void visit(Jump node) {
        as.jmp(node.label());
        return null;
    }

    @Override
    public Void visit(LabelStmt node) {
        as.label(node.label());
        return null;
    }

    @Override
    public Void visit(Mem node) {
        compile(node.expr());
        load(mem(ax()), ax(node.type()));
        return null;
    }

    @Override
    public Void visit(Return node) {
        if(node.expr() != null){
            compile(node.expr());
        }
        as.jmp(epilogue);
        return null;
    }

    @Override
    public Void visit(Str node) {
        loadConstant(node, ax());
        return null;
    }

    @Override
    public Void visit(Switch node) {
        compile(node.cond());
        Type t = node.cond().type();
        for(Case c : node.cases()){
            as.mov(imm(c.value), cx());
            as.cmp(cx(t), ax(t));
            as.je(c.label);
        }

        as.jmp(node.defaultLabel());
        return null;
    }

    @Override
    public Void visit(Uni node) {
        Type src = node.expr().type();
        Type dest = node.type();

        if(dest == Type.Float32){
            compile(node.expr());
            switch (node.op()){
                case UMINUS:
                    as.negFloat();
                    break;
                case S_CAST:
                    as.i2f(ax(src));
                    break;
                case U_CAST:
                    as.i2f(ax(src));
                    break;
                default:
                    throw new Error("unknown unary operator: " + node.op());
            }
            return null;
        }else{
            compile(node.expr());
            switch (node.op()){
                case UMINUS:
                    as.neg(ax(src));
                    break;
                case BIT_NOT:
                    as.not(ax(src));
                    break;
                case NOT:
                    as.test(ax(src), ax(src));
                    as.sete(al());
                    as.movzx(al(), ax(dest));
                    break;
                case S_CAST:
                    as.movsx(ax(src), ax(dest));
                    break;
                case U_CAST:
                    if(node.expr().type() == Type.Float32){
                        as.f2i(ax(dest));
                    }else{
                        as.movzx(ax(src), ax(dest));
                    }
                    break;
                default:
                    throw new Error("unknown unary operator: " + node.op());
            }
        }
        return null;
    }

    @Override
    public Void visit(Var node) {
        if(node.type() == Type.Float32){
            loadVariable(node, ax());
        }else{
            loadVariable(node, ax());
        }
        return null;
    }

    public AssemblyCode generator(IR ir) {
        return null;
    }

    // #@@range/reg_dsls2{
    private Register ax() { return ax(naturalType); }
    private Register al() { return ax(Type.INT8); }
    private Register bx() { return bx(naturalType); }
    // #@@}
    private Register cx() { return cx(naturalType); }
    private Register cl() { return cx(Type.INT8); }
    private Register dx() { return dx(naturalType); }

    // #@@range/reg_dsls1{
    private Register ax(Type t) {
        return new Register(RegisterClass.AX, t);
    }

    private Register bx(Type t) {
        return new Register(RegisterClass.BX, t);
    }
    // #@@}

    private Register cx(Type t) {
        return new Register(RegisterClass.CX, t);
    }

    private Register dx(Type t) {
        return new Register(RegisterClass.DX, t);
    }

    private Register si() {
        return new Register(RegisterClass.SI, naturalType);
    }

    private Register di() {
        return new Register(RegisterClass.DI, naturalType);
    }

    private Register bp() {
        return new Register(RegisterClass.BP, naturalType);
    }

    private Register sp() {
        return new Register(RegisterClass.SP, naturalType);
    }
    private class StackFrameInfo {
        List<Register> saveRegs;
        long lvarSize;
        long tempSize;

        long saveRegsSize(){
            return saveRegs.size() *  STACK_WORD_SIZE;
        }

        long lvarOffset(){
            return saveRegsSize();
        }

        long tempOffset(){
            return saveRegsSize() + lvarSize;
        }

        long frameSize(){
            return saveRegsSize() + lvarSize + tempSize;
        }
    }
}
