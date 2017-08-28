package com.simon816.i15n.core.cpu;

public class Register8 implements Register {

    private byte val;

    public Register8() {}

    public Register8(int val) {
        set(val);
    }

    @Override
    public int max() {
        return 0xFF;
    }

    @Override
    public int negBit() {
        return 0x80;
    }

    @Override
    public int get() {
        return this.val;
    }

    @Override
    public void set(int value) {
        this.val = (byte) (value & 0xFF);
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
