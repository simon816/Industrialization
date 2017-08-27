package com.simon816.i15n.core.entity.display;

import java.awt.image.BufferedImage;

import com.simon816.i15n.core.entity.display.PixelBaseColor.PixelColor;

public interface FrameBuffer {

    int getWidth();

    int getHeight();

    byte getPixel(int x, int y);

    void setPixel(int x, int y, byte pixel);

    default PixelColor getPixelColor(int x, int y) {
        return PixelColor.from(getPixel(x, y));
    }

    default void setPixel(int x, int y, PixelColor color) {
        setPixel(x, y, color.value());
    }

    void clear(byte color);

    default void clear(PixelColor color) {
        clear(color.value());
    }

    default void drawImage(BufferedImage img) {
        int width = Math.min(img.getWidth(), getWidth());
        int height = Math.min(img.getHeight(), getHeight());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                PixelColor color = PixelBaseColor.PixelColor.estimate(rgb);
                setPixel(x, y, color);
            }
        }
    }

    default void copyTo(FrameBuffer target) {
        int maxWidth = Math.min(this.getWidth(), target.getWidth());
        int maxHeight = Math.min(this.getHeight(), target.getHeight());
        if (maxWidth == 0 || maxHeight == 0) {
            return;
        }
        for (int row = 0; row < maxHeight; row++) {
            for (int col = 0; col < maxWidth; col++) {
                target.setPixel(col, row, getPixel(col, row));
            }
        }
    }
}
