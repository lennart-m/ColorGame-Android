package de.lennartmeinhardt.game.colormatching;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableInt;
import android.graphics.Color;

import java.util.Random;

public class RgbColor extends BaseObservable {

    public final ObservableInt red = new ObservableInt();
    public final ObservableInt green = new ObservableInt();
    public final ObservableInt blue = new ObservableInt();

    public final ValueStepHandler redStepHandler = new ColorChannelValueStepHandler(red);
    public final ValueStepHandler greenStepHandler = new ColorChannelValueStepHandler(green);
    public final ValueStepHandler blueStepHandler = new ColorChannelValueStepHandler(blue);
    public final ValueStepHandler cyanStepHandler = new ValueStepHandler.Reverser(redStepHandler);
    public final ValueStepHandler magentaStepHandler = new ValueStepHandler.Reverser(greenStepHandler);
    public final ValueStepHandler yellowStepHandler = new ValueStepHandler.Reverser(blueStepHandler);


    public RgbColor() {
        OnPropertyChangedCallback colorUpdater = new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                notifyPropertyChanged(BR.rgb);
            }
        };
        red.addOnPropertyChangedCallback(colorUpdater);
        green.addOnPropertyChangedCallback(colorUpdater);
        blue.addOnPropertyChangedCallback(colorUpdater);
    }


    @Bindable
    public int getRgb() {
        return Color.rgb(red.get(), green.get(), blue.get());
    }

    public void setRgb(int rgb) {
        red.set(Color.red(rgb));
        green.set(Color.green(rgb));
        blue.set(Color.blue(rgb));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RgbColor rgbColor = (RgbColor) o;

        if (!red.equals(rgbColor.red)) return false;
        if (!green.equals(rgbColor.green)) return false;
        return blue.equals(rgbColor.blue);

    }

    @Override
    public int hashCode() {
        int result = red.hashCode();
        result = 31 * result + green.hashCode();
        result = 31 * result + blue.hashCode();
        return result;
    }

    public void trimToDifficulty(Difficulty difficulty) {
        setRgb(trimColorToDifficulty(getRgb(), difficulty));
    }

    public static int trimColorToDifficulty(int rgb, Difficulty difficulty) {
        return Color.rgb(
                trimChannelToDifficulty(Color.red(rgb), difficulty),
                trimChannelToDifficulty(Color.green(rgb), difficulty),
                trimChannelToDifficulty(Color.blue(rgb), difficulty)
        );
    }

    private static int trimChannelToDifficulty(int value, Difficulty difficulty) {
        float relativeValue = value / 255f;
        int steps = Math.round(relativeValue * difficulty.getStepCount());
        return steps * difficulty.getStepSize();
    }

    public static int generateDifferentColor(Random random, int color, Difficulty difficulty) {
        int stepSize = difficulty.getStepSize();
        int stepCount = difficulty.getStepCount();
        int max = stepCount + 1;
        int red, green, blue, newColor;

        do {
            red = random.nextInt(max) * stepSize;
            green = random.nextInt(max) * stepSize;
            blue = random.nextInt(max) * stepSize;
            newColor = Color.rgb(red, green, blue);
        } while(newColor == color);
        return newColor;
    }


    private static class ColorChannelValueStepHandler implements ValueStepHandler {

        private final ObservableInt channel;

        public ColorChannelValueStepHandler(ObservableInt channel) {
            this.channel = channel;
        }

        @Override
        public void increaseBy(int steps) {
            stepBy(steps);
        }

        @Override
        public void decreaseBy(int steps) {
            stepBy(-steps);
        }

        private void stepBy(int steps) {
            int newValue = trimChannelValue(channel.get() + steps);
            channel.set(newValue);
        }

        private static int trimChannelValue(int value) {
            return Math.max(0, Math.min(255, value));
        }
    }
}
