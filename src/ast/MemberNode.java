package ast;


import type.CompositeType;
import type.Type;

public class MemberNode extends LHSNode{
    private ExprNode expr;
    private String member;

    public MemberNode(ExprNode expr, String member){
        this.expr = expr;
        this.member = member;
    }

    public CompositeType baseType(){
        try{
            return expr.type().getCompositeType();
        }catch (ClassCastException err){
            throw new Error(err.getMessage());
        }
    }

    public ExprNode expr(){
        return expr;
    }

    public String member(){
        return member;
    }

    public long offset(){
        return baseType().memberOffset(member);
    }

    protected Type origType(){
        return baseType().memberType(member);
    }
}
