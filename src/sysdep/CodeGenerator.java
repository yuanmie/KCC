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

    @Override
    public Void visit(Addr addr) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }

    @Override
    public Void visit(Bin bin) {
        return null;
    }

    @Override
    public Void visit(Call call) {
        return null;
    }

    @Override
    public Void visit(CJump cJump) {
        return null;
    }

    @Override
    public Void visit(ExprStmt exprStmt) {
        return null;
    }

    @Override
    public Void visit(Int anInt) {
        return null;
    }

    @Override
    public Void visit(Float aFloat) {
        return null;
    }

    @Override
    public Void visit(Jump jump) {
        return null;
    }

    @Override
    public Void visit(LabelStmt labelStmt) {
        return null;
    }

    @Override
    public Void visit(Mem mem) {
        return null;
    }

    @Override
    public Void visit(Return aReturn) {
        return null;
    }

    @Override
    public Void visit(Str str) {
        return null;
    }

    @Override
    public Void visit(Switch aSwitch) {
        return null;
    }

    @Override
    public Void visit(Uni uni) {
        return null;
    }

    @Override
    public Void visit(Var var) {
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
