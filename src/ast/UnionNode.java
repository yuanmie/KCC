package ast;

import type.Type;
import type.TypeRef;
import type.UnionType;

import java.util.List;

public class UnionNode extends CompositeTypeDefinition{
    public UnionNode(TypeRef ref, String name, List<Slot> membs){
        super(ref, name, membs);
    }

    public String kind(){
        return "union";
    }

    public boolean isUnion(){
        return true;
    }

    public Type definingType(){
        return new UnionType(name(), members());
    }

    public <T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
