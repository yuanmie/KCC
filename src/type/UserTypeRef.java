package type;

public class UserTypeRef extends TypeRef{
    protected String name;

    public UserTypeRef(String name) {
        super();
        this.name = name;
    }

    public boolean isUserType() {
        return true;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof UserTypeRef)){
            return false;
        }else{
            return name.equals(((UserTypeRef)other).name);
        }
    }

    public String toString() {
        return name;
    }
}
