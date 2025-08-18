import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int ptr;
    private int offset;
    private int temp_num;
    private int label_num;
    private SymbolTable symbolTable;
    private List<IR> code;
    private int[] labelToSeq;
    private Type t;
    private int IR_num; // 即将生成的指令的序号

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        ptr = 0;
        offset = 0;
        temp_num = 0;
        label_num = -1;
        symbolTable = new SymbolTable();
        code = new ArrayList<>();
        labelToSeq = new int[10000];
        IR_num = 0;

        Stmts(-100, -100);
        bindLabelWithAddress();
    }

    public void match(int attribute) {
        if (tokens.get(ptr).getAttribute() == attribute)
            ++ptr;
        else throw new SyntaxError("Syntax Error in line " + tokens.get(ptr).getLine() + ".");
    }

    public void error(int line) {
        throw new SyntaxError("Syntax Error in line " + line + ".");
    }

    public void notDeclared(String id, int line) {
        throw new SyntaxError("\"" + id + "\" is not declared in line " + line + ".");
    }

    public int new_temp() {
        return ++temp_num;
    }

    public int new_label() {
        return ++label_num;
    }

    public void gen(int op, IR.Address arg1, IR.Address arg2, IR.Address result) {
        code.add(new IR(IR_num, op, arg1, arg2, result));
        ++IR_num;
    }

    public void label(int l) {
        labelToSeq[l] = IR_num;
    }

    public void enterBlock() {
        SymbolTable table = new SymbolTable();
        table.setOut(symbolTable);
        symbolTable = table;
    }

    public void exitBlock() {
        symbolTable = symbolTable.getOut();
    }

    public List<IR> getCode() { return code; }

    public void Stmts(int break_goto, int continue_goto) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == 'i' || attr == 'r' || attr == 'd'
                || attr == Token.IF || attr == Token.WHILE || attr == Token.FOR
                || attr == Token.BREAK || attr == Token.CONTINUE
                || attr == '{') {
            S(break_goto, continue_goto);
            Stmts(break_goto, continue_goto);
        }
    }

    public void S(int break_goto, int continue_goto) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == 'i' || attr == 'r') D();
        else if (attr == 'd') A();
        else if (attr == Token.IF) {
            int If_next = new_label();
            If(If_next, break_goto, continue_goto);
            label(If_next);
        }
        else if (attr == Token.WHILE) {
            int While_next = new_label();
            While(While_next);
            label(While_next);
        }
        else if (attr == Token.FOR) {
            int For_next = new_label();
            For(For_next);
            label(For_next);
        }
        else if (attr == Token.BREAK) {
            if (break_goto == -100)
                error(tokens.get(ptr).getLine());
            match(Token.BREAK);
            match(';');
            gen(IR.JUMP, null, null, new IR.InstructionAddress(break_goto));
        }
        else if (attr == Token.CONTINUE) {
            if (continue_goto == -100)
                error(tokens.get(ptr).getLine());
            match(Token.CONTINUE);
            match(';');
            gen(IR.JUMP, null, null, new IR.InstructionAddress(continue_goto));
        }
        else {
            match('{');
            enterBlock();
            Stmts(break_goto, continue_goto);
            match('}');
            exitBlock();
        }
    }

    public void D() {
        Type T_type = T();
        Token token = tokens.get(ptr);
        match('d');
        match(';');
        symbolTable.enter(token.getValue(), T_type, offset);
        offset += T_type.getWidth();
    }

    public Type T() {
        t = B();
        return C();
    }

    public Type B() {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == 'i') {
            match('i');
            return new IntType();
        }
        else {
            match('r');
            return new RealType();
        }
    }

    public Type C() {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '[') {
            match('[');
            Token num = tokens.get(ptr);
            match(Token.CONST_INT);
            match(']');
            Type C1_type = C();
            return new ArrayType(Integer.parseInt(num.getValue()), C1_type);
        }
        else return t;
    }

    public void A() {
        Token id = tokens.get(ptr);
        match('d');
        if (tokens.get(ptr).getAttribute() == '=') {
            match('=');
            IR.Address E_addr_syn = E();
            match(';');
            if (!symbolTable.lookup(id.getValue()))
                notDeclared(id.getValue(), id.getLine());
            gen(IR.ASSIGN, E_addr_syn, null, new IR.VariableAddress(id.getValue()));
        }
        else {
            --ptr;
            IR.Address L_syn = L();
            match('=');
            IR.Address E_addr_syn = E();
            match(';');
            gen(IR.ASSIGN, E_addr_syn, null, L_syn);
        }
    }

    public void A_without_semicolon() {
        Token id = tokens.get(ptr);
        match('d');
        if (tokens.get(ptr).getAttribute() == '=') {
            match('=');
            IR.Address E_addr_syn = E();
            if (!symbolTable.lookup(id.getValue()))
                notDeclared(id.getValue(), id.getLine());
            gen(IR.ASSIGN, E_addr_syn, null, new IR.VariableAddress(id.getValue()));
        }
        else {
            --ptr;
            IR.Address L_syn = L();
            match('=');
            IR.Address E_addr_syn = E();
            gen(IR.ASSIGN, E_addr_syn, null, L_syn);
        }
    }

    public IR.Address E() {
        IR.Address E__addr_inh = Term();
        return E_(E__addr_inh);
    }

    public IR.Address E_(IR.Address addr_inh) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '+') {
            match('+');
            IR.Address T_addr_syn = Term();
            IR.Address E_1_addr_inh = new IR.TempAddress(new_temp());
            gen(IR.PLUS, addr_inh, T_addr_syn, E_1_addr_inh);
            return E_(E_1_addr_inh);
        }
        else if (attr == '-') {
            match('-');
            IR.Address T_addr_syn = Term();
            IR.Address E_1_addr_inh = new IR.TempAddress(new_temp());
            gen(IR.SUBTRACT, addr_inh, T_addr_syn, E_1_addr_inh);
            return E_(E_1_addr_inh);
        }
        else return addr_inh;
    }

    public IR.Address Term() {
        IR.Address F_addr_syn = F();
        return Term_(F_addr_syn);
    }

    public IR.Address Term_(IR.Address addr_inh) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '*') {
            match('*');
            IR.Address F_addr_syn = F();
            IR.Address T_1_addr_inh = new IR.TempAddress(new_temp());
            gen(IR.MULTIPLY, addr_inh, F_addr_syn, T_1_addr_inh);
            return Term_(T_1_addr_inh);
        }
        else if (attr == '/') {
            match('/');
            IR.Address F_addr_syn = F();
            IR.Address T_1_addr_inh = new IR.TempAddress(new_temp());
            gen(IR.DIVIDE, addr_inh, F_addr_syn, T_1_addr_inh);
            return Term_(T_1_addr_inh);
        }
        else return addr_inh;
    }

    public IR.Address F() {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '(') {
            match('(');
            IR.Address E_addr_syn = E();
            match(')');
            return E_addr_syn;
        }
        else if (attr == '-') {
            match('-');
            int attr1 = tokens.get(ptr).getAttribute();
            if (attr1 == '(') {
                match('(');
                IR.Address E_addr_syn = E();
                match(')');
                IR.Address F_addr_syn = new IR.TempAddress(new_temp());
                gen(IR.MINUS, E_addr_syn, null, F_addr_syn);
                return F_addr_syn;
            }
            else {
                IR.Address T_addr_syn = Term();
                IR.Address F_addr_syn = new IR.TempAddress(new_temp());
                gen(IR.MINUS, T_addr_syn, null, F_addr_syn);
                return F_addr_syn;
            }
        }
        else if (attr == Token.CONST_INT) {
            Token token = tokens.get(ptr);
            match(Token.CONST_INT);
            IR.Address F_addr_syn = new IR.ConstInteger(Integer.parseInt(token.getValue()));
            return F_addr_syn;
        }
        else if (attr == Token.CONST_REAL) {
            Token token = tokens.get(ptr);
            match(Token.CONST_REAL);
            IR.Address F_addr_syn = new IR.ConstReal(Double.parseDouble(token.getValue()));
            return F_addr_syn;
        }
        else {
            Token id = tokens.get(ptr);
            match('d');
            int attr1 = tokens.get(ptr).getAttribute();
            if (attr1 == '[') {
                --ptr;
                IR.Address L_syn = L();
                IR.Address F_addr_syn = new IR.TempAddress(new_temp());
                gen(IR.ASSIGN, L_syn, null, F_addr_syn);
                return F_addr_syn;
            }
            else {
                if (!symbolTable.lookup(id.getValue()))
                    notDeclared(id.getValue(), id.getLine());
                return new IR.VariableAddress(id.getValue());
            }
        }
    }

    public IR.Address L() {
        Token id = tokens.get(ptr);
        match('d');
        match('[');
        IR.Address E_addr_syn = E();
        match(']');
        Type temp_type = symbolTable.lookupType(id.getValue());
        if (temp_type == null)
            notDeclared(id.getValue(), id.getLine());
        Type L__type = ((ArrayType)temp_type).getElem();
        IR.Address L__offset = L_(L__type);
        IR.Address L_array = new IR.VariableAddress(id.getValue());
        IR.Address t = new IR.TempAddress(new_temp());
        gen(IR.MULTIPLY, E_addr_syn, new IR.ConstInteger(L__type.getWidth()), t);
        IR.Address L_offset = new IR.TempAddress(new_temp());
        gen(IR.PLUS, t, L__offset, L_offset);
        return new IR.ArrayAddress(L_array, L_offset);
    }

    public IR.Address L_(Type type) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '[') {
            match('[');
            IR.Address E_addr_syn = E();
            match(']');
            Type L_1_type = ((ArrayType)type).getElem();
            IR.Address L_1_offset = L_(L_1_type);
            IR.Address t = new IR.TempAddress(new_temp());
            gen(IR.MULTIPLY, E_addr_syn, new IR.ConstInteger(L_1_type.getWidth()), t);
            IR.Address L__offset = new IR.TempAddress(new_temp());
            gen(IR.PLUS, t, L_1_offset, L__offset);
            return L__offset;
        }
        else {
            IR.Address L__offset = new IR.ConstInteger(0);
            return L__offset;
        }
    }

    public void Bool(int B_true, int B_false) {
        int A_true = B_true;
        int A_false = new_label();
        And(A_true, A_false);
        label(A_false);
        Bool_(B_true, B_false);
    }

    public void Bool_(int B__true, int B__false) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '|') {
            match('|');
            int A_true = B__true;
            int A_false = new_label();
            And(A_true, A_false);
            label(A_false);
            Bool_(B__true, B__false);
        }
        else gen(IR.JUMP, null, null, new IR.InstructionAddress(B__false));
    }

    public void And(int A_true, int A_false) {
        int O_true = new_label();
        int O_false = A_false;
        Only(O_true, O_false);
        label(O_true);
        And_(A_true, A_false);
    }

    public void And_(int A__true, int A__false) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '&') {
            match('&');
            int O_true = new_label();
            int O_false = A__false;
            Only(O_true, O_false);
            label(O_true);
            And_(A__true, A__false);
        }
        else gen(IR.JUMP, null, null, new IR.InstructionAddress(A__true));
    }

    public void Only(int O_true, int O_false) {
        int attr = tokens.get(ptr).getAttribute();
        if (attr == '(') {
            match('(');
            Bool(O_true, O_false);
            match(')');
        }
        else if (attr =='!') {
            match('!');
            int attr1 = tokens.get(ptr).getAttribute();
            if (attr1 == '(') {
                match('(');
                Bool(O_false, O_true);
                match(')');
            }
            else Only(O_false, O_true);
        }
        else if (attr == 't') {
            match('t');
            gen(IR.JUMP, null, null, new IR.InstructionAddress(O_true));
        }
        else if (attr == 'f') {
            match('f');
            gen(IR.JUMP, null, null, new IR.InstructionAddress(O_false));
        }
        else {
            IR.Address E1_addr_syn = E();
            Token relop = tokens.get(ptr);
            match(Token.RELOP);
            IR.Address E2_addr_syn = E();
            int op;
            if (relop.getValue().equals("=="))
                op = IR.JUMP_EQUAL;
            else if (relop.getValue().equals("!="))
                op = IR.JUMP_NE;
            else if (relop.getValue().equals(">"))
                op = IR.JUMP_GT;
            else if (relop.getValue().equals(">="))
                op = IR.JUMP_GE;
            else if (relop.getValue().equals("<"))
                op = IR.JUMP_LT;
            else op = IR.JUMP_LE;
            gen(op, E1_addr_syn, E2_addr_syn, new IR.InstructionAddress(O_true));
            gen(IR.JUMP, null, null, new IR.InstructionAddress(O_false));
        }
    }

    public void While(int While_next) {
        match(Token.WHILE);
        int While_begin = new_label();
        label(While_begin);
        int B_true = new_label();
        int B_false = While_next;
        Bool(B_true, B_false);
        match(Token.DO);
        label(B_true);
        enterBlock();
        S(While_next, While_begin);
        exitBlock();
        gen(IR.JUMP, null, null, new IR.InstructionAddress(While_begin));
    }

    public void For(int For_next) {
        match(Token.FOR);
        match('(');
        A_without_semicolon();
        match(';');
        int B_true = new_label();
        int B_false = For_next;
        int S2_next = new_label();
        label(S2_next);
        Bool(B_true, B_false);
        match(';');
        int S3_next = new_label();
        label(S3_next);
        A_without_semicolon();
        gen(IR.JUMP, null, null, new IR.InstructionAddress(S2_next));
        match(')');
        label(B_true);
        enterBlock();
        S(For_next, S3_next);
        exitBlock();
        gen(IR.JUMP, null, null, new IR.InstructionAddress(S3_next));
    }

    public void If(int If_next, int break_goto, int continue_goto) {
        match(Token.IF);
        int B_true = new_label();
        int B_false = new_label();
        Bool(B_true, B_false);
        match(Token.THEN);
        label(B_true);
        enterBlock();
        S(break_goto, continue_goto);
        exitBlock();
        int attr = tokens.get(ptr).getAttribute();
        if (attr == Token.ELSE) {
            gen(IR.JUMP, null, null, new IR.InstructionAddress(If_next));
            match(Token.ELSE);
            label(B_false);
            enterBlock();
            S(break_goto, continue_goto);
            exitBlock();
        }
        else label(B_false);
    }

    public void bindLabelWithAddress() {
        for (IR ir : code) {
            if (ir.getOp() >= 6 && ir.getOp() <= 12) { // ir是跳转指令
                int l = ((IR.InstructionAddress)ir.getResult()).getNum();
                int addr = labelToSeq[l];
                ((IR.InstructionAddress)ir.getResult()).setNum(addr);
            }
        }
    }
}
