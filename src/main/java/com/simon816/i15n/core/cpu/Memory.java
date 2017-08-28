package com.simon816.i15n.core.cpu;

public interface Memory {

    byte readB(int addr);

    default short readH(int addr) {
        return (short) (readB(addr) << 8 | readB(addr + 1));
    }

    default long readN(int addr, int n) {
        long v = 0;
        while (n-- > 0) {
            v |= readB(addr) << n;
        }
        return v;
    }

    void writeB(int addr, byte val);

    default void writeH(int addr, short val) {
        writeB(addr, (byte) (val >>> 8));
        writeB(addr, (byte) (val & 0xFF));
    }

}
