package type;

public class IntegerTypeRef extends TypeRef{
    static public IntegerTypeRef charRef() {
        return new IntegerTypeRef("char");
    }


    static public IntegerTypeRef shortRef( ) {
        return new IntegerTypeRef("short");
    }



    static public IntegerTypeRef intRef( ) {
        return new IntegerTypeRef("int");
    }


    static public IntegerTypeRef longRef( ) {
        return new IntegerTypeRef("long");
    }



    static public IntegerTypeRef ucharRef( ) {
        return new IntegerTypeRef("unsigned char");
    }



    static public IntegerTypeRef ushortRef( ) {
        return new IntegerTypeRef("unsigned short");
    }



    static public IntegerTypeRef uintRef( ) {
        return new IntegerTypeRef("unsigned int");
    }


    static public IntegerTypeRef ulongRef() {
        return new IntegerTypeRef("unsigned long");
    }


    protected String name;
    

    public IntegerTypeRef(String name) {
        super();
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (! (other instanceof IntegerTypeRef)) return false;
        IntegerTypeRef ref = (IntegerTypeRef)other;
        return name.equals(ref.name);
    }
}
