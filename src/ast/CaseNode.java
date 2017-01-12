package ast;

import asm.Label;

import java.util.List;

public class CaseNode extends StmtNode{
    //dosomething
    protected List<ExprNode> values;
    protected BlockNode body;
    protected Label label;

    public CaseNode(List<ExprNode> values, BlockNode body){
        super();
        this.values = values;
        this.body = body;
        this.label = new Label();
    }

    public List<ExprNode> values(){
        return values;
    }

    public boolean isDefault(){
        return values.isEmpty();
    }

    public BlockNode body(){
        return body;
    }

    @Override
    public <S, E> S accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public Label label() {
        return label;
    }
}
