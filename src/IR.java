public class IR { // Intermediate Representation
    private int seqNum;
    private int op;
    private Address arg1;
    private Address arg2;
    private Address result;

    public static abstract class Address { }

    public static class VariableAddress extends Address {
        private String id;

        public VariableAddress(String id) {
            this.id = id;
        }

        public String getId() { return id; }

        @Override
        public String toString() { return id; }
    }

    public static class TempAddress extends Address {
        private int tempNum;

        public TempAddress(int tempNum) {
            this.tempNum = tempNum;
        }

        public int getTempNum() { return tempNum; }

        @Override
        public String toString() { return "t" + tempNum; }
    }

    public static class ArrayAddress extends Address {
        private Address array;
        private Address offset;

        public ArrayAddress(Address array, Address offset) {
            this.array = array;
            this.offset = offset;
        }

        public Address getArray() { return array; }

        public Address getOffset() { return offset; }

        @Override
        public String toString() {
            return array.toString() + " [ " + offset.toString() + " ]";
        }
    }

    public static class ConstInteger extends Address {
        private int value;

        public ConstInteger(int value) { this.value = value; }

        public int getValue() { return value; }

        @Override
        public String toString() { return String.valueOf(value); }
    }

    public static class ConstReal extends Address {
        private double value;

        public ConstReal(double value) { this.value = value; }

        public double getValue() { return value; }

        @Override
        public String toString() { return String.valueOf(value); }
    }

    public static class InstructionAddress extends Address {
        private int num; // 指令序号（地址）

        public InstructionAddress(int num) { this.num = num; }

        public void setNum(int num) {this.num = num;}

        public int getNum() { return num; }

        @Override
        public String toString() { return String.valueOf(num); }
    }

    public IR(int seqNum, int op, Address arg1, Address arg2, Address result) {
        this.seqNum = seqNum;
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    public int getSeqNum() { return seqNum; }
    public int getOp() { return op; }
    public Address getArg1() { return arg1; }
    public Address getArg2() { return arg2; }
    public Address getResult() { return result; }

    // op
    public static int ASSIGN = 0, MINUS = 1, PLUS = 2, SUBTRACT = 3, MULTIPLY = 4, DIVIDE = 5,
            JUMP = 6, JUMP_EQUAL = 7, JUMP_NE = 8, JUMP_GT = 9, JUMP_GE = 10,
            JUMP_LT = 11, JUMP_LE = 12;

    @Override
    public String toString() {
        StringBuilder code = new StringBuilder();
        code.append(seqNum).append(":\t\t");
        if (op == ASSIGN)
            code.append(result.toString()).append(" = ").append(arg1.toString());
        else if (op == MINUS)
            code.append(result.toString()).append(" = minus ").append(arg1.toString());
        else if (op == PLUS)
            code.append(result.toString()).append(" = ").append(arg1.toString()).append(" + ").append(arg2.toString());
        else if (op == SUBTRACT)
            code.append(result.toString()).append(" = ").append(arg1.toString()).append(" - ").append(arg2.toString());
        else if (op == MULTIPLY)
            code.append(result.toString()).append(" = ").append(arg1.toString()).append(" * ").append(arg2.toString());
        else if (op == DIVIDE)
            code.append(result.toString()).append(" = ").append(arg1.toString()).append(" / ").append(arg2.toString());
        else if (op == JUMP)
            code.append("goto ").append(result.toString());
        else if (op == JUMP_EQUAL)
            code.append("if ").append(arg1.toString()).append(" == ").append(arg2.toString())
                    .append(" goto ").append(result.toString());
        else if (op == JUMP_NE)
            code.append("if ").append(arg1.toString()).append(" ！= ").append(arg2.toString())
                    .append(" goto ").append(result.toString());
        else if (op == JUMP_GT)
            code.append("if ").append(arg1.toString()).append(" > ").append(arg2.toString())
                    .append(" goto ").append(result.toString());
        else if (op == JUMP_GE)
            code.append("if ").append(arg1.toString()).append(" >= ").append(arg2.toString())
                    .append(" goto ").append(result.toString());
        else if (op == JUMP_LT)
            code.append("if ").append(arg1.toString()).append(" < ").append(arg2.toString())
                    .append(" goto ").append(result.toString());
        else code.append("if ").append(arg1.toString()).append(" <= ").append(arg2.toString())
                    .append(" goto ").append(result.toString());
        return code.toString();
    }
}
