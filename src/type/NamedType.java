package type;

abstract public class NamedType extends Type{
    protected String name;
    public NamedType(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
