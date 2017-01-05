package ast;

public class GotoNode extends StmtNode{
    protected String target;

    public GotoNode(String target){
        super();
        this.target = target;
    }

    public String target(){
        return target;
    }
}
