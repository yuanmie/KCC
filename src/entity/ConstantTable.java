package entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConstantTable implements Iterable<ConstantEntry> {
    protected Map<String, ConstantEntry> table;
    private String identifier = "#yuanmie";

    public ConstantTable(){
        table = new LinkedHashMap<String, ConstantEntry>();
    }


    public ConstantEntry intern(String s){
        ConstantEntry ent = table.get(s);
        if(ent == null){
            ent = new ConstantEntry(s);
            table.put(s, ent);
        }

        return ent;
    }

    public Collection<ConstantEntry> entries(){
        return table.values();
    }

    public Iterator<ConstantEntry> iterator(){
        return table.values().iterator();
    }

    //float constant
    public ConstantEntry intern(long s){
        ConstantEntry ent = table.get(identifier+String.valueOf(s));
        if (ent == null) {
            ent = new ConstantEntry(s);
            table.put(identifier+String.valueOf(s), ent);
        }
        return ent;
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }
}
