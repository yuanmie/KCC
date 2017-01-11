package ir;

abstract public class Stmt {
    abstract public <S, E> S accept(IRVisitor<S, E> visitor);
}
