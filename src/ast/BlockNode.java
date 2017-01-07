package ast;

import entity.DefinedVariable;
import entity.LocalScope;

import java.util.List;

public class BlockNode extends StmtNode{
    protected List<DefinedVariable> variables;
    protected List<StmtNode> stmts;
    protected LocalScope scope;

    public BlockNode(List<DefinedVariable> vars, List<StmtNode> stmts){
        super();
        this.variables = vars;
        this.stmts = stmts;
    }

    public List<DefinedVariable> variables(){
        return variables;
    }

    public List<StmtNode> stmts(){
        return stmts;
    }

    public StmtNode tailStmt(){
        if(stmts.isEmpty()){
            return null;
        }

        return stmts.get(stmts.size() - 1);
    }

    public LocalScope scope(){
        return scope;
    }

    public void setScope(LocalScope scope){
        this.scope = scope;
    }
}
