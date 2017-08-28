package com.simon816.i15n.core.cpu;

import com.simon816.i15n.core.ITickable;

public class CPU implements ITickable {

    private static final int S_CARRY = 1;
    private static final int S_ZERO = 2;
    private static final int S_NEG = 4;
    private static final int S_OVERFLOW = 8;

    private Register pc;
    private Register acc;
    private Register status;
    private Memory memory;

    public CPU(Memory memory, int initialPC) {
        this.memory = memory;
        this.pc = new Register16(initialPC);
        this.acc = new Register8();
        this.status = new Register8();
    }

    private short getAtPCInc() {
        return this.memory.readB(this.pc.getAndInc());
    }

    private void addWithCarry(int val) {
        int old = this.acc.get();
        int res = old + val + this.status.getbit(S_CARRY);
        this.acc.set(res);
        updateNZ(res);
        this.status.setbit(S_CARRY, res > this.acc.max());
        this.status.setbit(S_OVERFLOW, (((res ^ old) & (res ^ val) & this.acc.negBit()) != 0));
    }


    @Override
    public void tick() {
        int insn = getAtPCInc();
        switch (Opcode.get(insn)) {
            case ADC:
                addWithCarry(getAtPCInc());
                break;
            case AND:
                and(getAtPCInc());
                break;
            case ASL_ACC:
                shiftAccLeft();
                break;
            case ASL_MEM:
                shiftLeft(getAtPCInc());
                break;
            case BCC:
                branchIf(this.status.getbit(S_CARRY) == 0);
                break;
            case BCS:
                branchIf(this.status.getbit(S_CARRY) != 0);
                break;
        }
    }

    private void branchIf(boolean cond) {
        byte rel = this.memory.readB(this.pc.getAndInc()); // Allow signed
        if (cond) {
            this.pc.set(this.pc.get() + rel);
        }
    }

    private void shiftAccLeft() {
        int old = this.acc.get();
        int val = old << 1;
        this.acc.set(val);
        this.status.setbit(S_CARRY, (old & this.acc.negBit()) != 0);
        updateNZ(val);
    }

    private void shiftLeft(int addr) {
        byte old = this.memory.readB(addr);
        byte val = (byte) (old << 1);
        this.memory.writeB(addr, val);
        this.status.setbit(S_CARRY, (old & this.acc.negBit()) != 0);
        updateNZ(val);
    }

    private void and(int val) {
        this.acc.set(this.acc.get() & val);
    }

    private void updateNZ(int val) {
        this.status.setbit(S_ZERO, val == 0);
        this.status.setbit(S_NEG, val < 0);
    }

}
