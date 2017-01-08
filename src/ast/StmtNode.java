package ast;

abstract public class StmtNode extends Node{
    public StmtNode(){

    }

    abstract public <S,E> S accept(ASTVisitor<S,E> visitor);
}
