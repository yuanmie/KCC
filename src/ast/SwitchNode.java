package ast;

import java.util.List;

public class SwitchNode extends StmtNode{
    protected ExprNode cond;
    protected List<CaseNode> cases;

    public SwitchNode(ExprNode cond, List<CaseNode> cases){
        super();
        this.cond = cond;
        this.cases = cases;
    }

    public ExprNode cond(){
        return cond;
    }

    public List<CaseNode> cases(){
        return cases;
    }
}
