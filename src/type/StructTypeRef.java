package type;

public class StructTypeRef extends TypeRef {
    protected String name;

    public StructTypeRef(String name) {
        super();
        this.name = name;
    }

    public boolean isStruct() {
        return true;
    }

    public boolean equals(Object other) {
        if (!(other instanceof StructTypeRef)){
            return false;
        }else{
            return name.equals(((StructTypeRef)other).name);
        }

    }

    public String name() {
        return name;
    }

    public String toString() {
        return "struct " + name;
    }
}
