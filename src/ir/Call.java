package ir;

import asm.Type;
import entity.Entity;
import entity.Function;

import java.util.List;

public class Call extends Expr {
    private Expr expr;
    private List<Expr> args;

    public Call(Type type, Expr expr, List<Expr> args){
        super(type);
        this.expr = expr;
        this.args = args;
    }

    public Expr expr(){
        return expr;
    }

    public List<Expr> args(){
        return args;
    }

    public long numArgs(){
        return args.size();
    }

    public boolean isStaticCall(){
        return (expr.getEntityForce() instanceof Function);
    }

    public Function function(){
        Entity ent = expr.getEntityForce();
        if(ent == null){
            throw new Error("not a static funcall");
        }
        return (Function)ent;
    }

    public boolean isVariadic(){
        Entity entity = expr.getEntityForce();
        return entity.isVariadic();
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
