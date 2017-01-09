package compiler;

import ast.*;
import entity.CBCParameter;
import entity.DefinedFunction;
import entity.DefinedVariable;
import type.FunctionType;
import type.IntegerType;
import type.Type;
import type.TypeTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeChecker extends Visitor{

    private final TypeTable typeTable;


    public TypeChecker(TypeTable types) {
        this.typeTable = types;
    }

    private void check(StmtNode node){
        visitStmt(node);
    }

    private void check(ExprNode node){
        visitExpr(node);
    }
    DefinedFunction currentFunction;

    public void check(AST ast){
        for(DefinedVariable var : ast.definedVariables()){
            checkVariable(var);
        }

        for(DefinedFunction f : ast.definedFunctions()){
            currentFunction = f;
            checkReturnType(f);
            checkParamTypes(f);
            check(f.body());
        }
    }

    private void checkVariable(DefinedVariable var) {
        if(isInvalidVariableType(var.type())){
            throw new Error("invalid variable type");
        }

        if(var.hasInitializer()){
            if(isInvalidLHSType(var.type())){
                throw new Error("invalid LHS type: " + var.type());
            }
            check(var.initializer());
            var.setInitializer(implicitCast(var.type(), var.initializer()));
        }
    }

    private ExprNode implicitCast(Type targetType, ExprNode expr) {
        //deal array/struct/union init
        if(expr instanceof InitNode){
            if(targetType.isArray() || targetType.isStruct() || targetType.isUnion()){
                return expr;
            }else{
                throw new Error("{init} is only allow array, struct ,union");
            }
        }
        //same type do nothing
        else if(expr.type().isSameType(targetType)){
            return expr;
        }
        //cast
        else if(expr.type().isCastableTo(targetType)){
            if(!expr.type().isCompatible(targetType) && !isSafeIntegerCast(expr, targetType)){
                System.out.println("Incompatible implicit cast from " + expr.type() + " to " +targetType);
            }
            return new CastNode(targetType, expr);
        }else{
            //cast error
            invalidCastError(expr, expr.type(), targetType);
            return expr;
        }
    }

    private boolean isSafeIntegerCast(Node node, Type type) {
        if(!type.isInteger()){
            return false;
        }
        IntegerType t = (IntegerType)type;
        if(!(node instanceof IntegerLiteralNode)){
            return false;
        }

        return t.isInDomain(((IntegerLiteralNode) node).value());
    }

    private void invalidCastError(Node n, Type l, Type r) {
        throw new Error("invalid cast from " + l + " to " + r);
    }

    private boolean isInvalidLHSType(Type type) {
        return type.isVoid();
    }

    private boolean isInvalidVariableType(Type type) {
        return type.isVoid();
    }

    //check each param
    private void checkParamTypes(DefinedFunction f) {
        for(CBCParameter param : f.parameters()){
            if(isInvalidParamterType(param.type())){
                throw new Error("invalid paramter type: " + param.type());
            }
        }
    }

    private boolean isInvalidParamterType(Type t) {
        return t.isStruct() || t.isUnion() || t.isVoid()
                || t.isIncompleteArray();
    }

    private void checkReturnType(DefinedFunction f) {
        if(isInvalidReturnType(f.returnType())){
            throw new Error("return invalid type: " + f.returnType());
        }
    }

    //does not support struct/union return
    private boolean isInvalidReturnType(Type t) {
        return t.isStruct() || t.isUnion() || t.isArray();
    }

    public Void visit(BlockNode node){
        for(DefinedVariable var : node.variables()){
            checkVariable(var);
        }
        for(StmtNode n : node.stmts()){
            check(n);
        }

        return null;
    }

    public Void visit(ExprStmtNode node){
        check(node.expr());
        if(isInvalidStatementType(node.expr().type())){
            throw new Error("invalid statement type: " + node.expr().type());
        }
        return null;
    }

    private boolean isInvalidStatementType(Type t) {
        return t.isStruct() || t.isUnion();
    }

    public Void visit(IfNode node){
        super.visit(node);
        checkCond(node.cond());
        return null;
    }

    //condition node must be scalar
    private void checkCond(ExprNode cond) {
        mustBeScalar(cond, "condition expresion");
    }

    private boolean mustBeScalar(ExprNode expr, String s) {
        if(!expr.type().isScalar()){
            wrongTypeError(expr, s);
            return false;
        }
        return true;
    }

    public Void visit(WhileNode node){
        super.visit(node);
        checkCond(node.cond());
        return null;
    }

    public Void visit(ForNode node){
        super.visit(node);
        checkCond(node.cond());
        return null;
    }

    public Void visit(SwitchNode node){
        super.visit(node);
        mustBeInteger(node.cond(), "condition expression");
        return null;
    }

    public Void visit(ReturnNode node){
        super.visit(node);
        if(currentFunction.isVoid()){
            if(node.expr() != null){
                throw new Error(node + " return valud from void function");
            }
        }
        else{
            if(node.expr() == null){
                throw new Error(node + "missing return value");
            }
            if(node.expr().type().isVoid()){
                throw new Error(node + "return void");
            }
            node.setExpr(implicitCast(currentFunction.returnType(), node.expr()));
        }
        return null;
    }

    public Void visit(AssignNode node){
        super.visit(node);
        if(!checkLHS(node.lhs())){
            return null;
        }
        if(!checkRHS(node.rhs())){
            return null;
        }
        node.setRHS(implicitCast(node.lhs().type(), node.rhs()));
        return null;
    }

    private boolean checkRHS(ExprNode rhs) {
        if(isInvalidRHSType(rhs.type())){
            throw new Error("invalid RHS expression type: " + rhs.type());
        }
        return true;
    }

    private boolean isInvalidRHSType(Type t) {
        return t.isVoid();
    }

    public Void visit(OpAssignNode node){
        super.visit(node);
        if(!checkLHS(node.lhs())){
            return null;
        }

        if(!checkRHS(node.rhs())){
            return null;
        }

        if(node.operator().equals("+") || node.operator().equals("-")){
            if(node.lhs().type().isPointer()){
                mustBeInteger(node.rhs(), node.operator());
                node.setRHS(integralPromotedExpr(node.rhs()));
                return null;
            }
        }


        Type l = Promotion(node.lhs().type());
        Type r = Promotion(node.rhs().type());
        Type opType = usualArithmeticConversion(l, r);
        if(!opType.isCompatible(l) && !isSafeCast(node.rhs(), opType)){
            System.out.println("warn: incompatible implicit cast from " + opType + "to" + l);
        }
        if(!r.isSameType(opType)){
            node.setRHS(new CastNode(opType, node.rhs()));
        }
        return null;
    }

    private boolean isSafeCast(Node node, Type type){
        if(type.isFloat()){
            return true;
        }else if(type.isInteger()){
            if(node instanceof FloatLiteralNode){
                return false;
            }
            return isSafeIntegerCast(node, type);
        }else{
            return isSafeIntegerCast(node, type);
        }
    }

    private boolean checkLHS(ExprNode lhs){
        if(lhs.isParameter()){
            return true;
        }else if(isInvalidLHSType(lhs.type())){
            throw new Error(lhs + " invalid LHS expression type: " + lhs.type());
        }
        return true;
    }

    public Void visit(CondExprNode node){
        super.visit(node);
        checkCond(node.cond());
        Type t = node.thenExpr().type();
        Type e = node.elseExpr().type();
        if(t.isSameType(e)){
            return null;
        }
        else if(t.isCompatible(e)){
            node.setThenExpr(new CastNode(e, node.thenExpr()));
        }
        else if(e.isCompatible(t)){
            node.setElseExpr(new CastNode(t, node.elseExpr()));
        }else{
            invalidCastError(node.thenExpr(), e, t);
        }
        return null;
    }

    public Void visit(BinaryOpNode node){
        super.visit(node);
        if(node.operator().equals("+") || node.operator().equals("-")){
            expectsSameIntegerOrFloatOrPointerDiff(node);
        }
        else if (node.operator().equals("*")
                || node.operator().equals("/")
                || node.operator().equals("%")
                || node.operator().equals("&")
                || node.operator().equals("|")
                || node.operator().equals("^")
                || node.operator().equals("<<")
                || node.operator().equals(">>")) {
            expectsSameIntegerOrFloat(node);
        }
        else if (node.operator().equals("==")
                || node.operator().equals("!=")
                || node.operator().equals("<")
                || node.operator().equals("<=")
                || node.operator().equals(">")
                || node.operator().equals(">=")) {
            expectsComparableScalars(node);
        }
        else {
            throw new Error("unknown binary operator: " + node.operator());
        }
        return null;
    }

    private void expectsComparableScalars(BinaryOpNode node) {
        if(!mustBeScalar(node.left(), node.operator())){
            return ;
        }
        if(!mustBeScalar(node.right(), node.operator())){
            return;
        }

        if(node.left().type().isPointer()){
            ExprNode right = forcePointerType(node.left(), node.right());
            node.setRight(right);
            node.setType(node.left().type());
            return;
        }

        if(node.right().type().isPointer()){
            ExprNode left = forcePointerType(node.right(), node.left());
            node.setLeft(left);
            node.setType(node.right().type());
            return;
        }

        arithmeticImplicitCast(node);
    }

    private ExprNode forcePointerType(ExprNode master, ExprNode slave) {
        if(master.type().isCompatible(slave.type())){
            return slave;
        }else{
            System.out.println("incompatible implicit cast from "
                    + slave.type() + " to " + master.type());
            return new CastNode(master.type(), slave);
        }
    }


    private void expectsSameIntegerOrFloatOrPointerDiff(BinaryOpNode node) {
        if(node.left().isPointer() && node.right().isPointer()){
            if(node.operator().equals("+")){
                throw new Error(node + "invalid operation: pointer + pointer");
            }
            node.setType(typeTable.ptrDiffType());
        }
        else if(node.left().isPointer()){
            mustBeInteger(node.right(), node.operator());
            node.setRight(integralPromotedExpr(node.right()));
            node.setType(node.left().type());
        }else if(node.right().isPointer()){
            if(node.operator().equals("-")){
                throw new Error(node + "invalid operation: integer - pointer");
            }

            mustBeInteger(node.left(), node.operator());
            node.setLeft(integralPromotedExpr(node.left()));
            node.setType(node.right().type());
        }else{
            expectsSameIntegerOrFloat(node);
        }
    }

    private ExprNode integralPromotedExpr(ExprNode expr){
        Type t = integralPromotion(expr.type());
        if(t.isSameType(expr.type())){
            return expr;
        }else{
            return new CastNode(t, expr);
        }
    }

    private void expectsSameIntegerOrFloat(BinaryOpNode node){
        Type left = node.left().type();
        Type right = node.right().type();
        if(node.left().type().isFloat() && node.right().type().isFloat()){
            return ;
        }
        if(node.left().type().isFloat() && node.right().type().isInteger()){
            return ;
        }
        if(node.left().type().isInteger() && node.right().type().isFloat()){
            return ;
        }

        if(!mustBeInteger(node.left(), node.operator())){
            return;
        }

        if(!mustBeInteger(node.right(), node.operator())){
            return ;
        }

        arithmeticImplicitCast(node);
    }

    private void arithmeticImplicitCast(BinaryOpNode node) {
        Type r = Promotion(node.right().type());
        Type l = Promotion(node.left().type());
        Type target = usualArithmeticConversion(l, r);

        if(!l.isSameType(target)){
            node.setLeft(new CastNode(target, node.left()));
        }

        if(!r.isSameType(target)){
            node.setRight(new CastNode(target, node.right()));
        }
        node.setType(target);
    }

    private Type usualArithmeticConversion(Type l, Type r) {
        Type s_int = typeTable.signedInt();
        Type u_int = typeTable.unsignedInt();
        Type s_long = typeTable.signedLong();
        Type u_long = typeTable.unsignedLong();
        Type floatType = typeTable.floatType();
        //add float feature support
        if(l.isSameType(floatType) && r.isSameType(floatType)){
            return floatType;
        }else if(l.isSameType(floatType) || r.isSameType(floatType)){
            return floatType;
        }
        else if ((l.isSameType(u_int) && r.isSameType(s_long))
                || (r.isSameType(u_int) && l.isSameType(s_long))) {
            return u_long;
        }
        else if (l.isSameType(u_long) || r.isSameType(u_long)) {
            return u_long;
        }
        else if (l.isSameType(s_long) || r.isSameType(s_long)) {
            return s_long;
        }
        else if (l.isSameType(u_int)  || r.isSameType(u_int)) {
            return u_int;
        }
        else {
            return s_int;
        }
    }

    private Type Promotion(Type t) {
        if(t.isFloat()){
            return t;
        }else{
            return integralPromotion(t);
        }
    }

    private Type integralPromotion(Type t) {
        if(!t.isInteger()){
            throw new Error("integralPromotion for " + t);
        }
        Type intType = typeTable.signedInt();
        if(t.size() < intType.size()){
            return intType;
        }else{
            return t;
        }
    }

    private boolean mustBeInteger(ExprNode expr, String op) {
        if(!expr.type().isInteger()){
            wrongTypeError(expr, op);
            return false;
        }
        return true;
    }

    private void wrongTypeError(ExprNode expr, String op) {
        throw new Error("wrong operand type for " + op + " : " + expr.type());
    }

    public Void visit(LogicalAndNode node){
        super.visit(node);
        expectsComparableScalars(node);
        return null;
    }

    public Void visit(LogicalOrNode node){
        super.visit(node);
        expectsComparableScalars(node);
        return null;
    }


    public Void visit(UnaryOpNode node){
        super.visit(node);
        if(node.operator().equals("!")){
            mustBeScalar(node.expr(), node.operator());
        }else{
            if(node.expr().type().isFloat()){
                return null;
            }else{
                mustBeInteger(node.expr(), node.operator());
            }
        }
        return null;
    }

    public Void visit(PrefixOpNode node){
        super.visit(node);
        expectsScalarLHS(node);
        return null;
    }

    public Void visit(SuffixOpNode node){
        super.visit(node);
        expectsScalarLHS(node);
        return null;
    }


    private void expectsScalarLHS(UnaryArithmeticOpNode node) {
        if(node.expr().isParameter()){

        }else if(node.expr().type().isArray()){
            wrongTypeError(node.expr(), node.operator());
            return ;
        }else{
            mustBeScalar(node.expr(), node.operator());
        }
        if(node.expr().type().isInteger()){
            Type opType = integralPromotion(node.expr().type());
            if(!node.expr().type().isSameType(opType)){
                node.setOpType(opType);
            }
            node.setAmount(1);
        }else if(node.expr().type().isPointer()){
            if(node.expr().type().baseType().isVoid()){
                wrongTypeError(node.expr(), node.operator());
                return;
            }
            node.setAmount(node.expr().type().baseType().size());
        }else{
            throw new Error("must not happen");
        }
    }


    public Void visit(FuncallNode node){
        super.visit(node);
        FunctionType type = node.functionType();
        if(!type.acceptsArgs(node.numArgs())){
            throw new Error("wrong number of arguments" + node.numArgs());
        }

        Iterator<ExprNode> args = node.args().iterator();
        List<ExprNode> newArgs = new ArrayList<>();

        for(Type param : type.paramTypes()){
            ExprNode arg = args.next();
            newArgs.add(checkRHS(arg) ? implicitCast(param, arg) : arg);
        }

        while(args.hasNext()){
            ExprNode arg = args.next();
            newArgs.add(checkRHS(arg) ? castOptionalArg(arg) : arg);
        }
        node.replaceArgs(newArgs);
        return null;
    }

    private ExprNode castOptionalArg(ExprNode arg) {
        if(arg.type().isFloat()){
            //dosomething
        }
        if(!arg.type().isInteger()){
            return arg;
        }
        Type t = arg.type().isSigned() ? typeTable.signedStackType()
                : typeTable.unsignedStackType();
        return arg.type().size() < t.size() ? implicitCast(t, arg) : arg;
    }

    public Void visit(ArefNode node){
        super.visit(node);
        mustBeInteger(node.index(), "[]");
        return null;
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
}
