package ast;

import type.Type;

public class CondExprNode extends ExprNode{
    protected ExprNode cond, thenExpr, elseExpr;

    public CondExprNode(ExprNode cond, ExprNode thenExpr,
                        ExprNode elseExpr) {
        this.cond = cond;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    public Type type() {
        return thenExpr.type();
    }

    public ExprNode cond() {
        return cond;
    }

    public ExprNode thenExpr() {
        return thenExpr;
    }

    public void setThenExpr(ExprNode expr) {
        this.thenExpr = expr;
    }

    public ExprNode elseExpr() {
        return elseExpr;
    }

    public void setElseExpr(ExprNode expr) {
        this.elseExpr = expr;
    }
}
