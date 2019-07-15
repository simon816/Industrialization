package com.simon816.i15n.silicon.monitor;

import java.util.Arrays;

public class ArrayFrameBuffer implements FrameBuffer {

    private final byte[][] pixelData;
    private final int width;

    public ArrayFrameBuffer(int cols, int rows) {
        this.pixelData = new byte[rows][cols];
        this.width = cols;
    }

    @Override
    public int getHeight() {
        return this.pixelData.length;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public byte getPixel(int x, int y) {
        return this.pixelData[y][x];
    }

    @Override
    public void setPixel(int x, int y, byte pixel) {
        this.pixelData[y][x] = pixel;
    }

    @Override
    public void clear(byte color) {
        for (byte[] row : this.pixelData) {
            Arrays.fill(row, color);
        }
    }

}
