package ast;

import type.CompositeType;
import type.PointerType;
import type.Type;

public class PtrMemberNode extends LHSNode{
    public ExprNode expr;
    public String member;

    public PtrMemberNode(ExprNode expr, String member){
        this.expr = expr;
        this.member = member;
    }

    public CompositeType dereferedCompositeType(){
        try{
            PointerType pt = expr.type().getPointerType();
            return pt.baseType().getCompositeType();
        }catch (ClassCastException err){
            throw new Error(err.getMessage());
        }
    }

    public Type dereferdType(){
        try{
            PointerType pt = expr.type().getPointerType();
            return pt.baseType();
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
        return dereferedCompositeType().memberOffset(member);
    }

    protected Type origType(){
        return dereferedCompositeType().memberType(member);
    }


}
