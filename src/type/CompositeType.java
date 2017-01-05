package type;


import ast.Slot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract public class CompositeType extends NamedType{
    protected List<Slot> members;
    protected long cachedSize;
    protected long cachedAlign;
    protected boolean isRecursiveChecked;

    public CompositeType(String name, List<Slot> members){
        super(name);
        this.members = members;
        this.cachedSize = Type.sizeUnknown;
        this.cachedAlign = Type.sizeUnknown;
        this.isRecursiveChecked = false;
    }

    public boolean isCompositeType(){
        return true;
    }

    protected boolean compareMemberTypes(Type other,
                                         String cmpMethod){
        if(isStruct() && !other.isStruct()){
            return false;
        }

        if(isUnion() && !other.isUnion()){
            return false;
        }

        CompositeType otherType = other.getCompositeType();
        if(members.size() != other.size()){
            return false;
        }

        Iterator<Type> otherTypes =
                otherType.memberTypes().iterator();
        for(Type t : memberTypes()){
            if(!compareTypeBy(cmpMethod, t, otherTypes.next())){
                return false;
            }
        }
        return true;
    }

    protected boolean compareTypeBy(String cmpMethod, Type t, Type tt){
        try{
            Method cmp = Type.class.getMethod(cmpMethod,new Class[]{Type.class});
            Boolean b = (Boolean)cmp.invoke(t, new Object[]{tt});
            return b.booleanValue();
        }catch (NoSuchMethodException e){
            throw new Error(e.getMessage());
        }catch (IllegalAccessException e){
            throw new Error(e.getMessage());
        }catch(InvocationTargetException e){
            throw new Error(e.getMessage());
        }
    }

    public List<Slot> members(){
        return members;
    }

    public List<Type> memberTypes(){
        List<Type> result = new ArrayList<Type>();
        for(Slot s : members){
            result.add(s.type());
        }

        return result;
    }

    @Override
    public long size() {
        if (cachedSize == Type.sizeUnknown) {
            computeOffsets();
        }
        return cachedSize;
    }

    public long alignmemt() {
        if (cachedAlign == Type.sizeUnknown) {
            computeOffsets();
        }
        return cachedAlign;
    }

    public boolean hasMember(String name){
        return (get(name) != null);
    }

    public Type memberType(String name){
        return fetch(name).type();
    }

    public long memberOffset(String name) {
        Slot s = fetch(name);
        if (s.offset() == Type.sizeUnknown) {
            computeOffsets();
        }
        return s.offset();
    }

    abstract protected void computeOffsets();

    protected Slot fetch(String name){
        Slot s = get(name);
        if(s == null){
            throw new Error("no such member in " + toString()
            + " : " + name);
        }
        return s;
    }

    public Slot get(String name){
        for(Slot s : members){
            if(s.name().equals(name)){
                return s;
            }
        }
        return null;
    }
    @Override
    public boolean isSameType(Type other) {
        return false;
    }

    @Override
    public boolean isCompatible(Type other) {
        return false;
    }

    @Override
    public boolean isCastableTo(Type target) {
        return false;
    }
}
