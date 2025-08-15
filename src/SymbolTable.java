import java.util.*;

public class SymbolTable {
    private SymbolTable out;
    private Map<String, Object[]> symbolTable; // id, type, offset

    public SymbolTable() {
        out = null;
        symbolTable = new HashMap<>();
    }

    public void enter(String id, Type type, int offset) {
        symbolTable.put(id, new Object[]{type, offset});
    }

    public Type lookupType(String id) {
        for (SymbolTable table = this; table != null; table = table.out) {
            if (table.symbolTable.containsKey(id))
                return (Type)table.symbolTable.get(id)[0];
        }
        return null;
    }

    public Integer lookupOffset(String id) {
        for (SymbolTable table = this; table != null; table = table.out) {
            if (table.symbolTable.containsKey(id))
                return (Integer)table.symbolTable.get(id)[1];
        }
        return null;
    }

    public boolean lookup(String id) {
        for (SymbolTable table = this; table != null; table = table.out) {
            if (table.symbolTable.containsKey(id))
                return true;
        }
        return false;
    }

    public void setOut(SymbolTable out) { this.out = out; }

    public SymbolTable getOut() { return out; }
}
