package ast;

import type.IntegerType;
import type.IntegerTypeRef;

public class ForNode extends StmtNode{
    protected StmtNode init;
    protected ExprNode cond;
    protected StmtNode incr;
    protected StmtNode body;

    public ForNode(ExprNode init, ExprNode cond,
                   ExprNode incr, StmtNode body){
        super();
        if(init != null){
            this.init = new ExprStmtNode(init);
        }else{
            this.init = null;
        }

        if(cond != null){
            this.cond = cond;
        }else{
            this.cond = new IntegerLiteralNode(IntegerTypeRef.intRef(), 1);
        }

        if(incr != null){
            this.incr = new ExprStmtNode(incr);
        }else{
            this.incr = null;
        }

        this.body = body;
    }

    public StmtNode init() {
        return init;
    }

    public ExprNode cond() {
        return cond;
    }

    public StmtNode incr() {
        return incr;
    }

    public StmtNode body() {
        return body;
    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
