package type;

import ast.Slot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeTable {
    static public TypeTable ilp32(){
        return newTable(1, 2, 4, 4, 4);
    }

    static private TypeTable newTable(int charsize, int shortsize,
                                      int intsize, int longsize, int ptrsize){
        TypeTable table = new TypeTable(intsize , longsize, ptrsize);
        table.put(new VoidTypeRef(), new VoidType());
        table.put(IntegerTypeRef.charRef(),
                new IntegerType(charsize,  true, "char"));
        table.put(IntegerTypeRef.shortRef(),
                new IntegerType(shortsize, true, "short"));
        table.put(IntegerTypeRef.intRef(),
                new IntegerType(intsize, true, "int"));
        table.put(IntegerTypeRef.longRef(),
                new IntegerType(longsize, true, "long"));
        table.put(IntegerTypeRef.ucharRef(),
                new IntegerType(charsize, false, "unsigned char"));
        table.put(IntegerTypeRef.ushortRef(),
                new IntegerType(shortsize, false, "unsigned short"));
        table.put(IntegerTypeRef.uintRef(),
                new IntegerType(intsize, false, "unsigned int"));
        table.put(IntegerTypeRef.ulongRef(),
                new IntegerType(longsize, false, "unsigned long"));
        table.put(FloatTypeRef.floatRef(),
                new FloatType(longsize, "float"));
        return table;
    }

    private int intSize;
    private int longSize;
    private int pointerSize;
    private Map<TypeRef, Type> table;

    public TypeTable(int intSize, int longSize, int pointerSize) {
        this.intSize = intSize;
        this.longSize = longSize;
        this.pointerSize = pointerSize;
        this.table = new HashMap<TypeRef, Type>();
    }

    public boolean isDefined(TypeRef ref){
        return table.containsKey(ref);
    }

    public void put(TypeRef ref, Type type){
        if(table.containsKey(ref)){
            throw new Error("duplicated type definition: " + ref);
        }else{
            table.put(ref, type);
        }
    }

    public Type get(TypeRef ref){
        Type type = table.get(ref);
        if(type == null){
            if(ref instanceof UserTypeRef){
                UserTypeRef uref = (UserTypeRef)ref;
                throw new Error("undefined type: " + uref.name());
            }else if(ref instanceof PointerTypeRef){
                PointerTypeRef pref = (PointerTypeRef)ref;
                Type t = new PointerType(pointerSize, get(pref.baseType()));
                table.put(pref, t);
                return t;
            }else if(ref instanceof ArrayTypeRef){
                ArrayTypeRef aref = (ArrayTypeRef)ref;
                Type t = new ArrayType(get(aref.baseType()),
                        aref.length(), pointerSize);
                table.put(aref, t);
                return t;
            }else if(ref instanceof FunctionTypeRef){
                FunctionTypeRef fref = (FunctionTypeRef)ref;
                Type t = new FunctionType(get(fref.returnType()),
                        fref.params().internTypes(this));
                table.put(fref, t);
                return t;
            }
            throw new Error("unregister type: " + ref.toString());
        }
        return type;
    }

    public Type getParamType(TypeRef ref){
        Type type = get(ref);
        return type.isArray() ? pointerTo(type.baseType()) : type;
    }

    public int longSize() {
        return this.longSize;
    }

    public int pointerSize() {
        return this.pointerSize;
    }

    public int maxIntSize() {
        return this.pointerSize;
    }

    public Type ptrDiffType(){
        return get(ptrDiffTypeRef());
    }

    public TypeRef ptrDiffTypeRef(){
        return new IntegerTypeRef(ptrDiffTypeName());
    }

    protected String ptrDiffTypeName(){
        if(signedLong().size == pointerSize){
            return "long";
        }else if(signedInt().size == pointerSize){
            return "int";
        }else if(signedShort().size == pointerSize){
            return "short";
        }else{
            throw new Error("must not happen: integer.size != point.size");
        }
    }

    public Type signedStackType(){
        return signedLong();
    }

    public Type unsignedStackType(){
        return unsignedLong();
    }

    public Collection<Type> types(){
        return table.values();
    }

    public VoidType voidType() {
        return (VoidType)table.get(new VoidTypeRef());
    }

    public IntegerType signedChar() {
        return (IntegerType)table.get(IntegerTypeRef.charRef());
    }

    public IntegerType signedShort() {
        return (IntegerType)table.get(IntegerTypeRef.shortRef());
    }

    public IntegerType signedInt() {
        return (IntegerType)table.get(IntegerTypeRef.intRef());
    }

    public IntegerType signedLong() {
        return (IntegerType)table.get(IntegerTypeRef.longRef());
    }

    public IntegerType unsignedChar() {
        return (IntegerType)table.get(IntegerTypeRef.ucharRef());
    }

    public IntegerType unsignedShort() {
        return (IntegerType)table.get(IntegerTypeRef.ushortRef());
    }

    public IntegerType unsignedInt() {
        return (IntegerType)table.get(IntegerTypeRef.uintRef());
    }

    public FloatType floatType() {
        return (FloatType)table.get(FloatTypeRef.floatRef());
    }

    public IntegerType unsignedLong() {
        return (IntegerType)table.get(IntegerTypeRef.ulongRef());
    }

    public PointerType pointerTo(Type baseType) {
        return new PointerType(pointerSize, baseType);
    }

    public void semanticCheck() {
        for(Type t : types()){
            if(t instanceof CompositeType){
                checkVoidMembers((CompositeType)t);
                checkDuplicatedMembers((CompositeType)t);
            }else if(t instanceof ArrayType){
                checkVoidMembers((ArrayType)t);
            }
            checkRecursiveDefinition(t);
        }
    }

    protected void checkVoidMembers(ArrayType t) {
        if(t.baseType().isVoid()){
            throw new Error("array cannot contain void");
        }
    }

    protected void checkVoidMembers(CompositeType t){
        for(Slot s : t.members()){
            if(s.type().isVoid()){
                throw new Error("struct/union cannot contain void");
            }
        }
    }

    protected void checkDuplicatedMembers(CompositeType t){
        Map<String, Slot> seen = new HashMap<>();
        for(Slot s : t.members()){
            if(seen.containsKey(s.name())){
                throw new Error(t.toString() + "has duplicated member: " + s.name());
            }else{
                seen.put(s.name(), s);
            }
        }
    }

    protected void checkRecursiveDefinition(Type t){
        _checkRecursiveDefinition(t, new HashMap<Type, Object>());
    }

    static final protected Object checking = new Object();
    static final protected Object checked = new Object();

    private void _checkRecursiveDefinition(Type t, HashMap<Type, Object> marks) {
        if(marks.get(t) == checking){
            throw new Error("recursive type definition: " + t);
        }else if(marks.get(t) == checked){
            return ;
        }else{
            marks.put(t, checking);
            if(t instanceof CompositeType){
                CompositeType ct = (CompositeType)t;
                for(Slot s : ct.members()){
                    _checkRecursiveDefinition(s.type(), marks);
                }
            }else if(t instanceof ArrayType){
                ArrayType at = (ArrayType)t;
                _checkRecursiveDefinition(at.baseType, marks);
            }else if(t instanceof UserType){
                UserType ut = (UserType)t;
                _checkRecursiveDefinition(ut.realType(), marks);
            }
            marks.put(t, checked);
        }
    }
}
