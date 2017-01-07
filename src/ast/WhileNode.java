package ast;

public class WhileNode extends StmtNode{
    protected StmtNode body;
    protected ExprNode cond;

    public WhileNode(ExprNode cond, StmtNode body){
        super();
        this.cond = cond;
        this.body = body;
    }

    public ExprNode cond(){
        return cond;
    }

    public StmtNode body(){
        return body;
    }
}
