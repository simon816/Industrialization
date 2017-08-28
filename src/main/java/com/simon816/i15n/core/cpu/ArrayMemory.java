package com.simon816.i15n.core.cpu;

public class ArrayMemory implements Memory {

    private byte[] data;

    public ArrayMemory(byte[] data) {
        this.data = data;
    }

    @Override
    public byte readB(int addr) {
        return this.data[addr];
    }

    @Override
    public void writeB(int addr, byte val) {
        this.data[addr] = val;
    }

}
