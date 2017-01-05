package ast;

import type.FunctionType;
import type.Type;

import java.util.List;

public class FuncallNode extends ExprNode{
    protected ExprNode expr;
    protected List<ExprNode> args;

    public FuncallNode(ExprNode expr, List<ExprNode> args){
        this.expr = expr;
        this.args = args;
    }

    public ExprNode expr(){
        return expr;
    }

    //return type;
    public Type type(){
        try{
            return functionType().returnType();
        }catch(ClassCastException err){
            throw new Error(err.getMessage());
        }
    }

    public FunctionType functionType(){
        return expr.type().getPointerType()
                .baseType().getFunctionType();
    }

    public long numArgs(){
        return args.size();
    }

    public void replaceArgs(List<ExprNode> args){
        this.args = args;
    }
}
