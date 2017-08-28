package com.simon816.i15n.core.cpu;

public class Register16 implements Register {

    private short val;

    public Register16() {}

    public Register16(int val) {
        set(val);
    }

    @Override
    public int max() {
        return 0xFFFF;
    }

    @Override
    public int negBit() {
        return 0x8000;
    }

    @Override
    public int get() {
        return this.val;
    }

    @Override
    public void set(int value) {
        this.val = (short) (value & 0xFFFF);
    }

    @Override
    public int getAndInc() {
        return this.val++;
    }

    @Override
    public void inc() {
        this.val++;
    }

}
