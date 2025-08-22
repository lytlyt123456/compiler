import java.util.*;

public class IROptimizer {
    private List<IR> code;
    private HashMap<Integer, IR.Address> constantValueTemp;

    public IROptimizer(List<IR> sourceIR) {
        this.code = sourceIR;
        constantValueTemp = new HashMap<>();

        constantFolding();
        constantPropagation();
        eliminateNull();
        eliminateGoto();
        eliminateNull();
        alignTempNum();
    }

    public List<IR> getCode() { return code; }

    public void constantFolding_single(IR ir) {
        if (ir.getOp() == IR.PLUS) {
            if (ir.getArg1() instanceof IR.ConstInteger
                    && ir.getArg2() instanceof IR.ConstInteger) {
                int val = ((IR.ConstInteger)ir.getArg1()).getValue()
                        + ((IR.ConstInteger)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstInteger(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstReal
                    && ir.getArg2() instanceof IR.ConstReal) {
                double val = ((IR.ConstReal)ir.getArg1()).getValue()
                        + ((IR.ConstReal)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstReal(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg1()).getValue() == 0
                    || ir.getArg1() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg1()).getValue() == 0.0) {
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, ir.getArg2(), null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg2() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg2()).getValue() == 0
                    || ir.getArg2() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg2()).getValue() == 0.0) {
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, ir.getArg1(), null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
        }
        else if (ir.getOp() == IR.SUBTRACT) {
            if (ir.getArg1() instanceof IR.ConstInteger
                    && ir.getArg2() instanceof IR.ConstInteger) {
                int val = ((IR.ConstInteger)ir.getArg1()).getValue()
                        - ((IR.ConstInteger)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstInteger(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstReal
                    && ir.getArg2() instanceof IR.ConstReal) {
                double val = ((IR.ConstReal)ir.getArg1()).getValue()
                        - ((IR.ConstReal)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstReal(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg1()).getValue() == 0
                    || ir.getArg1() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg1()).getValue() == 0.0) {
                IR newIR = new IR(ir.getSeqNum(), IR.MINUS, ir.getArg2(), null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg2() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg2()).getValue() == 0
                    || ir.getArg2() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg2()).getValue() == 0.0) {
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, ir.getArg1(), null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
        }
        else if (ir.getOp() == IR.MULTIPLY) {
            if (ir.getArg1() instanceof IR.ConstInteger
                    && ir.getArg2() instanceof IR.ConstInteger) {
                int val = ((IR.ConstInteger)ir.getArg1()).getValue()
                        * ((IR.ConstInteger)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstInteger(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstReal
                    && ir.getArg2() instanceof IR.ConstReal) {
                double val = ((IR.ConstReal)ir.getArg1()).getValue()
                        * ((IR.ConstReal)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstReal(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg1()).getValue() == 0
                    || ir.getArg1() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg1()).getValue() == 0.0) {
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, ir.getArg1(), null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg2() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg2()).getValue() == 0
                    || ir.getArg2() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg2()).getValue() == 0.0) {
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, ir.getArg2(), null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
        }
        else if (ir.getOp() == IR.DIVIDE) {
            if (ir.getArg1() instanceof IR.ConstInteger
                    && ir.getArg2() instanceof IR.ConstInteger
                    && ((IR.ConstInteger)ir.getArg2()).getValue() != 0) {
                int val = ((IR.ConstInteger)ir.getArg1()).getValue()
                        / ((IR.ConstInteger)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstInteger(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
            else if (ir.getArg1() instanceof IR.ConstReal
                    && ir.getArg2() instanceof IR.ConstReal
                    && ((IR.ConstReal)ir.getArg2()).getValue() != 0.0) {
                double val = ((IR.ConstReal)ir.getArg1()).getValue()
                        / ((IR.ConstReal)ir.getArg2()).getValue();
                IR.Address arg = new IR.ConstReal(val);
                IR newIR = new IR(ir.getSeqNum(), IR.ASSIGN, arg, null, ir.getResult());
                code.set(ir.getSeqNum(), newIR);
            }
        }
    }

    public void constantFolding() {
        for (int i = 0; i < code.size(); ++i) {
            IR ir = code.get(i);
            constantFolding_single(ir);
        }
    }

    public boolean eliminateConstantTemp(IR ir) { // 消除形如 "t1 = {const_num}" "t1 = t2" 临时变量赋值语句
        IR.Address arg = ir.getArg1();
        IR.TempAddress result = (IR.TempAddress)ir.getResult();
        if (arg instanceof IR.ConstInteger || arg instanceof IR.ConstReal) {
            constantValueTemp.put(result.getTempNum(), arg);
            code.set(ir.getSeqNum(), null);
            return true;
        }
        else if (arg instanceof IR.TempAddress) {
            int tempNum = ((IR.TempAddress)arg).getTempNum();
            if (constantValueTemp.containsKey(tempNum)) {
                IR.Address v = constantValueTemp.get(tempNum);
                constantValueTemp.put(result.getTempNum(), v);
                code.set(ir.getSeqNum(), null);
                return true;
            }
            else {
                constantValueTemp.put(result.getTempNum(), arg);
                code.set(ir.getSeqNum(), null);
                return true;
            }
        }
        return false;
    }

    public void constantPropagation() {
        for (int i = 0; i < code.size(); ++i) {
            IR ir = code.get(i);
            if (ir == null) continue;
            boolean eliminated = false;
            if (ir.getOp() == IR.ASSIGN && ir.getResult() instanceof IR.TempAddress) {
                eliminated = eliminateConstantTemp(ir);
            }
            if (!eliminated) {
                IR.Address arg1 = ir.getArg1();
                IR.Address arg2 = ir.getArg2();
                IR.Address result = ir.getResult();
                if (arg1 != null) {
                    if (arg1 instanceof IR.TempAddress) {
                        int tempNum = ((IR.TempAddress)arg1).getTempNum();
                        if (constantValueTemp.containsKey(tempNum)) {
                            IR.Address v = constantValueTemp.get(tempNum);
                            IR newIR = new IR(i, ir.getOp(), v, arg2, result);
                            code.set(i, newIR);
                        }
                    }
                    else if (arg1 instanceof IR.ArrayAddress) {
                        IR.ArrayAddress arg1_array = (IR.ArrayAddress)arg1;
                        IR.Address offset = arg1_array.getOffset();
                        if (offset instanceof IR.TempAddress) {
                            int tempNum = ((IR.TempAddress)offset).getTempNum();
                            if (constantValueTemp.containsKey(tempNum)) {
                                IR.Address v = constantValueTemp.get(tempNum);
                                IR.ArrayAddress aa = new IR.ArrayAddress(arg1_array.getArray(), v);
                                IR newIR = new IR(i, ir.getOp(), aa, arg2, result);
                                code.set(i, newIR);
                            }
                        }
                    }
                }
                ir = code.get(i);
                arg1 = ir.getArg1(); arg2 = ir.getArg2(); result = ir.getResult();
                if (arg2 != null) {
                    if (arg2 instanceof IR.TempAddress) {
                        int tempNum = ((IR.TempAddress)arg2).getTempNum();
                        if (constantValueTemp.containsKey(tempNum)) {
                            IR.Address v = constantValueTemp.get(tempNum);
                            IR newIR = new IR(i, ir.getOp(), arg1, v, result);
                            code.set(i, newIR);
                        }
                    }
                    else if (arg2 instanceof IR.ArrayAddress) {
                        IR.ArrayAddress arg2_array = (IR.ArrayAddress)arg2;
                        IR.Address offset = arg2_array.getOffset();
                        if (offset instanceof IR.TempAddress) {
                            int tempNum = ((IR.TempAddress)offset).getTempNum();
                            if (constantValueTemp.containsKey(tempNum)) {
                                IR.Address v = constantValueTemp.get(tempNum);
                                IR.ArrayAddress aa = new IR.ArrayAddress(arg2_array.getArray(), v);
                                IR newIR = new IR(i, ir.getOp(), arg1, aa, result);
                                code.set(i, newIR);
                            }
                        }
                    }
                }
                ir = code.get(i);
                arg1 = ir.getArg1(); arg2 = ir.getArg2(); result = ir.getResult();
                if (result instanceof IR.ArrayAddress) {
                    IR.ArrayAddress result_array = (IR.ArrayAddress)result;
                    IR.Address offset = result_array.getOffset();
                    if (offset instanceof IR.TempAddress) {
                        int tempNum = ((IR.TempAddress)offset).getTempNum();
                        if (constantValueTemp.containsKey(tempNum)) {
                            IR.Address v = constantValueTemp.get(tempNum);
                            IR.ArrayAddress aa = new IR.ArrayAddress(result_array.getArray(), v);
                            IR newIR = new IR(i, ir.getOp(), arg1, arg2, aa);
                            code.set(i, newIR);
                        }
                    }
                }
                ir = code.get(i);
                arg1 = ir.getArg1(); arg2 = ir.getArg2(); result = ir.getResult();
                constantFolding_single(ir);
                ir = code.get(i);
                if (ir.getOp() == IR.ASSIGN && ir.getResult() instanceof IR.TempAddress) {
                    eliminateConstantTemp(ir);
                }
            }
        }
    }

    public void eliminateNull() {
        HashMap<Integer, Integer> insSeqMap = new HashMap<>();
        List<IR> newCode = new ArrayList<>();
        int seq = -1;
        for (int i = 0; i < code.size(); ++i) {
            if (code.get(i) != null) {
                ++seq;
                for (int j = i - 1; j >= 0; --j) {
                    if (code.get(j) == null)
                        insSeqMap.put(j, seq);
                    else break;
                }
                insSeqMap.put(i, seq);
                newCode.add(code.get(i));
            }
        }
        ++seq;
        insSeqMap.put(code.size(), seq);
        code = newCode;
        for (int i = 0; i < code.size(); ++i) {
            IR ir = code.get(i);
            int newSeqNum = insSeqMap.get(ir.getSeqNum());
            if (ir.getOp() >= 6 && ir.getOp() <= 12) { // ir是跳转语句
                int target = insSeqMap.get(((IR.InstructionAddress)ir.getResult()).getNum());
                IR.Address newResult = new IR.InstructionAddress(target);
                IR newIR = new IR(newSeqNum, ir.getOp(), ir.getArg1(), ir.getArg2(), newResult);
                code.set(i, newIR);
            }
            else {
                IR newIR = new IR(newSeqNum, ir.getOp(), ir.getArg1(), ir.getArg2(), ir.getResult());
                code.set(i, newIR);
            }
        }
    }

    public void eliminateGoto() { // 消除冗余跳转指令
        // 消除非条件跳转指令后的冗余非条件跳转指令
        HashMap<Integer, Integer> mp = new HashMap<>();
        for (int i = code.size() - 1; i >= 1; --i) {
            IR ir = code.get(i);
            if (ir.getOp() == IR.JUMP && code.get(i - 1).getOp() == IR.JUMP) {
                mp.put(i, ((IR.InstructionAddress)ir.getResult()).getNum());
                code.set(i, null);
            }
        }
        for (int i = 0; i < code.size(); ++i) {
            IR ir = code.get(i);
            if (ir == null) continue;
            if (ir.getOp() >= 6 && ir.getOp() <= 12) {
                int target = ((IR.InstructionAddress)ir.getResult()).getNum();
                while (mp.containsKey(target))
                    target = mp.get(target);
                IR.InstructionAddress newResult = new IR.InstructionAddress(target);
                IR newIR = new IR(i, ir.getOp(), ir.getArg1(), ir.getArg2(), newResult);
                code.set(i, newIR);
            }
        }
        eliminateNull();
        // 消除条件跳转指令后的冗余非条件跳转指令
        for (int i = 0; i <= code.size() - 2; ++i) {
            IR ir = code.get(i);
            if (ir == null) continue;
            if (ir.getOp() >= 7 && ir.getOp() <= 12) { // ir是条件跳转指令
                IR ir_next = code.get(i + 1);
                int target_satisfied = ((IR.InstructionAddress)ir.getResult()).getNum();
                int target_unsatisfied = ((IR.InstructionAddress)ir_next.getResult()).getNum();
                if (target_satisfied == ir.getSeqNum() + 2) {
                    IR newIR = new IR(ir.getSeqNum(), IR.oppositeOperation(ir.getOp()),
                            ir.getArg1(), ir.getArg2(), ir_next.getResult());
                    code.set(i, newIR);
                    code.set(i + 1, null);
                }
                else if (target_unsatisfied == ir.getSeqNum() + 2)
                    code.set(i + 1, null);
            }
        }
    }

    public void alignTempNum() {
        HashMap<Integer, Integer> mp = new HashMap<>();
        int cnt = 0;
        for (int i = 0; i < code.size(); ++i) {
            IR ir = code.get(i);
            if (ir.getResult() instanceof IR.TempAddress) {
                ++cnt;
                int tempNum = ((IR.TempAddress)ir.getResult()).getTempNum();
                mp.put(tempNum, cnt);
            }
        }
        for (int i = 0; i < code.size(); ++i) {
            IR ir = code.get(i);
            if (ir.getArg1() != null) {
                if (ir.getArg1() instanceof IR.TempAddress) {
                    int tempNum = ((IR.TempAddress)ir.getArg1()).getTempNum();
                    int newTempNum = mp.get(tempNum);
                    IR.Address newArg1 = new IR.TempAddress(newTempNum);
                    code.set(i, new IR(i, ir.getOp(), newArg1, ir.getArg2(), ir.getResult()));
                }
                else if (ir.getArg1() instanceof IR.ArrayAddress) {
                    IR.Address offset = ((IR.ArrayAddress)ir.getArg1()).getOffset();
                    if (offset instanceof IR.TempAddress) {
                        int tempNum = ((IR.TempAddress)offset).getTempNum();
                        int newTempNum = mp.get(tempNum);
                        IR.ArrayAddress aa = new IR.ArrayAddress(((IR.ArrayAddress)ir.getArg1()).getArray(),
                                new IR.TempAddress(newTempNum));
                        code.set(i, new IR(i, ir.getOp(), aa, ir.getArg2(), ir.getResult()));
                    }
                }
            }
            ir = code.get(i);
            if (ir.getArg2() != null) {
                if (ir.getArg2() instanceof IR.TempAddress) {
                    int tempNum = ((IR.TempAddress)ir.getArg2()).getTempNum();
                    int newTempNum = mp.get(tempNum);
                    IR.Address newArg2 = new IR.TempAddress(newTempNum);
                    code.set(i, new IR(i, ir.getOp(), ir.getArg1(), newArg2, ir.getResult()));
                }
                else if (ir.getArg2() instanceof IR.ArrayAddress) {
                    IR.Address offset = ((IR.ArrayAddress)ir.getArg2()).getOffset();
                    if (offset instanceof IR.TempAddress) {
                        int tempNum = ((IR.TempAddress)offset).getTempNum();
                        int newTempNum = mp.get(tempNum);
                        IR.ArrayAddress aa = new IR.ArrayAddress(((IR.ArrayAddress)ir.getArg2()).getArray(),
                                new IR.TempAddress(newTempNum));
                        code.set(i, new IR(i, ir.getOp(), ir.getArg1(), aa, ir.getResult()));
                    }
                }
            }
            ir = code.get(i);
            if (ir.getResult() instanceof IR.TempAddress) {
                int tempNum = ((IR.TempAddress)ir.getResult()).getTempNum();
                int newTempNum = mp.get(tempNum);
                IR.Address newResult = new IR.TempAddress(newTempNum);
                code.set(i, new IR(i, ir.getOp(), ir.getArg1(), ir.getArg2(), newResult));
            }
            else if (ir.getResult() instanceof IR.ArrayAddress) {
                IR.Address offset = ((IR.ArrayAddress)ir.getResult()).getOffset();
                if (offset instanceof IR.TempAddress) {
                    int tempNum = ((IR.TempAddress)offset).getTempNum();
                    int newTempNum = mp.get(tempNum);
                    IR.ArrayAddress aa = new IR.ArrayAddress(((IR.ArrayAddress)ir.getResult()).getArray(),
                            new IR.TempAddress(newTempNum));
                    code.set(i, new IR(i, ir.getOp(), ir.getArg1(), ir.getArg2(), aa));
                }
            }
        }
    }
}
