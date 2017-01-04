package type;

public class UnionTypeRef extends TypeRef {
    protected String name;


    public UnionTypeRef(String name) {
        super();
        this.name = name;
    }

    public boolean isUnion() {
        return true;
    }

    public boolean equals(Object other) {
        if (!(other instanceof UnionTypeRef)){
            return false;
        }else{
            return name.equals(((UnionTypeRef)other).name);
        }
    }

    public String name() {
        return name;
    }

    public String toString() {
        return "union " + name;
    }
}
