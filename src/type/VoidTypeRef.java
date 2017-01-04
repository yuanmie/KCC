package type;

public class VoidTypeRef extends TypeRef{
    public VoidTypeRef() {
        super();
    }

    public boolean isVoid() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof VoidTypeRef);
    }

    public String toString() {
        return "void";
    }
}
