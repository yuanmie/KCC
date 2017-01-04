package entity;

import java.util.List;

abstract public class ParamSlots<T> {
    protected List<T> paramDescriptors;
    protected boolean vararg;

    public ParamSlots(List<T> paramDescs){
        this(paramDescs, false);
    }

    protected ParamSlots(List<T> paraDescs, boolean vararg){
        this.paramDescriptors = paraDescs;
        this.vararg = vararg;
    }

    public int argc(){
        if(vararg){
            throw new Error("must not happen: Param argc for vararg");
        }else{
            return paramDescriptors.size();
        }
    }

    public int minArgc(){
        return paramDescriptors.size();
    }

    public void acceptVarargs(){
        this.vararg = true;
    }

    public boolean isVararg(){
        return vararg;
    }

}
