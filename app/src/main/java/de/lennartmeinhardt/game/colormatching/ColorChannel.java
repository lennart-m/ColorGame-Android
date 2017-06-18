package de.lennartmeinhardt.game.colormatching;

import android.graphics.Color;

public enum ColorChannel {
    RED_CYAN(R.string.color_red, R.string.color_cyan) {
        @Override
        int getRgbValue(int rgb) {
            return Color.red(rgb);
        }
    },
    GREEN_MAGENTA(R.string.color_green, R.string.color_magenta) {
        @Override
        int getRgbValue(int rgb) {
            return Color.green(rgb);
        }
    },
    BLUE_YELLOW(R.string.color_blue, R.string.color_yellow) {
        @Override
        int getRgbValue(int rgb) {
            return Color.blue(rgb);
        }
    };

    private final int rgbChannelNameId, cmyChannelNameId;

    ColorChannel(int rgbChannelNameId, int cmyChannelNameId) {
        this.rgbChannelNameId = rgbChannelNameId;
        this.cmyChannelNameId = cmyChannelNameId;
    }

    abstract int getRgbValue(int rgb);

    int getCmyValue(int rgb) {
        return 255 - getRgbValue(rgb);
    }

    public int getValue(int rgb, boolean forRgb) {
        return forRgb ? getRgbValue(rgb) : getCmyValue(rgb);
    }

    public int getChannelNameId(boolean forRgb) {
        return forRgb ? rgbChannelNameId : cmyChannelNameId;
    }

    public int compareColorValues(int color1, int color2, boolean forRgb) {
        int value1 = getValue(color1, forRgb);
        int value2 = getValue(color2, forRgb);
        return value1 == value2 ? 0
                : value1 < value2 ? -1 : 1;
    }
}
