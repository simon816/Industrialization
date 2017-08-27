package com.simon816.i15n.core.entity.display;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

/*
 * Values taken from http://minecraft.gamepedia.com/Map_item_format#Color_table
 */
public enum PixelBaseColor {

    TRANSPARENT(-1, -1, -1),

    GRASS_GREEN(127, 178, 56),

    SAND(247, 233, 163),

    SILVER(199, 199, 199),

    RED(255, 0, 0),

    FAINT_BLUE(160, 160, 255),

    DARKER_SILVER(167, 167, 167),

    DARK_GREEN(0, 124, 0),

    WHITE(255, 255, 255),

    ALUMINIUM(164, 168, 184),

    DIRT_BROWN(151, 109, 77),

    MEDIUM_GRAY(112, 112, 112),

    BLUE(64, 64, 255),

    WOOD(143, 119, 72),

    WHITE_QUARTZ(255, 252, 245),

    ORANGE(216, 127, 51),

    MAGENTA(178, 76, 216),

    LIGHT_BLUE(102, 153, 216),

    YELLOW(229, 229, 51),

    LIME(127, 204, 25),

    PINK(242, 127, 165),

    DARK_GRAY(76, 76, 76),

    LIGHT_GRAY(153, 153, 153),

    CYAN(76, 127, 153),

    PURPLE(127, 63, 178),

    DEEP_BLUE(51, 76, 178),

    BROWN(102, 76, 51),

    BROWNISH_GREEN(102, 127, 51),

    FADED_RED(153, 51, 51),

    BLACK(25, 25, 25),

    GOLD(250, 238, 77),

    TURQUOISE_BLUE(92, 219, 213),

    PALE_BLUE(74, 128, 255),

    GREEN(0, 217, 58),

    LIGHT_BROWN(129, 86, 49),

    DARK_RED(112, 2, 0);

    final byte[] variants = new byte[4];
    final PixelColor[] colors = new PixelColor[this.variants.length];

    private PixelBaseColor(int r, int g, int b) {
        int base = ordinal();
        for (int i = 0; i < this.variants.length; i++) {
            this.variants[i] = (byte) (base * this.variants.length + i);
            this.colors[i] = new PixelColor(this, i, r, g, b);
        }
    }

    public PixelColor base() {
        return this.colors[2];
    }

    public PixelColor dark() {
        return this.colors[1];
    }

    public PixelColor darker() {
        return this.colors[0];
    }

    public PixelColor darkest() {
        return this.colors[3];
    }

    public byte baseVal() {
        return this.variants[2];
    }

    public byte darkVal() {
        return this.variants[1];
    }

    public byte darkerVal() {
        return this.variants[0];
    }

    public byte darkestVal() {
        return this.variants[3];
    }

    public static class PixelColor {

        private static final double[] MULTIPLIERS = new double[] {180, 220, 255, 135};

        public final PixelBaseColor base;
        private final int variant;
        private final Vector3i color;

        PixelColor(PixelBaseColor base, int variant, int r, int g, int b) {
            this.base = base;
            this.variant = variant;
            if (r == -1 || g == -1 || b == -1) {
                this.color = null;
            } else {
                this.color = new Vector3d(r, g, b).mul(MULTIPLIERS[variant]).div(255D).toInt();
            }
        }

        public byte value() {
            return this.base.variants[this.variant];
        }

        public Vector3i getColor() {
            return this.color;
        }

        @Override
        public String toString() {
            return new StringBuilder(this.base.toString()).append('[')
                    .append(this.variant)
                    .append(']').toString();
        }

        public static PixelColor from(byte val) {
            int base = val / 4;
            int variant = val % 4;
            return PixelBaseColor.values()[base].colors[variant];
        }

        public static PixelColor estimate(int rgb) {

            int r = rgb >>> 16 & 0xFF;
            int g = rgb >>> 8 & 0xFF;
            int b = rgb & 0xFF;

            PixelColor closest = TRANSPARENT.base();
            double closestDist = Double.MAX_VALUE;
            for (PixelBaseColor base : values()) {
                for (PixelColor color : base.colors) {
                    double dist;
                    if (color.color == null) {
                        dist = Double.MAX_VALUE;
                    } else {
                        dist = color.color.distanceSquared(r, g, b);
                    }
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = color;
                    }
                }
            }
            return closest;
        }

    }

}
