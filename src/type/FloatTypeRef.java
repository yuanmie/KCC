package type;

public class FloatTypeRef extends TypeRef {

    static public FloatTypeRef longDoubleRef() {
        return new FloatTypeRef("long double");
    }

    static public FloatTypeRef DoubleRef() {
        return new FloatTypeRef("Double");
    }

    static public FloatTypeRef floatRef() {
        return new FloatTypeRef("float");
    }


    protected String name;


    public FloatTypeRef(String name) {
        super();
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (! (other instanceof FloatTypeRef)){
            return false;
        }
        FloatTypeRef ref = (FloatTypeRef)other;
        return name.equals(ref.name);
    }

    public String toString() {
        return name;
    }
}
