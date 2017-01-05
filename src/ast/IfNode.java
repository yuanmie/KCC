package ast;

public class IfNode extends StmtNode{
    protected ExprNode cond;
    protected StmtNode thenBody;
    protected StmtNode elseBody;

    public IfNode(ExprNode c, StmtNode t, StmtNode e){
        super();
        this.cond = c;
        this.thenBody = t;
        this.elseBody = e;
    }

    public ExprNode cond(){
        return cond;
    }

    public StmtNode thenBody(){
        return thenBody;
    }

    public StmtNode elseBody(){
        return elseBody;
    }
}
