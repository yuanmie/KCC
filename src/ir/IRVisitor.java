package ir;

import entity.Jump;

public interface IRVisitor<S, E> {
    public E visit(Addr addr) ;

    public S visit(Assign assign);

    public E visit(Bin bin) ;

    public E visit(Call call);

    S visit(CJump cJump);

    S visit(ExprStmt exprStmt);

    E visit(Int anInt);

    E visit(Float aFloat);

    S visit(Jump jump);

    S visit(LabelStmt labelStmt);

    E visit(Mem mem);

    S visit(Return aReturn);

    E visit(Str str);

    S visit(Switch aSwitch);

    E visit(Uni uni);

    E visit(Var var);
}
