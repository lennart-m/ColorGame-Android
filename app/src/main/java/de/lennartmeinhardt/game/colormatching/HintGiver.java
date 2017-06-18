package de.lennartmeinhardt.game.colormatching;

import android.content.Context;

import java.util.Random;

public class HintGiver {

    private final Context context;

    private final Random random = new Random();

    public HintGiver(Context context) {
        this.context = context;
    }

    public String generateHintMessage(int currentRgb, int targetRgb, boolean forRgb) {

        ColorChannel[] allChannels = ColorChannel.values();
        // start checking a random index in 0, 1, 2
        int startIndex = random.nextInt(3);

        // check all three channels starting from the random index
        for(int i = 0; i < 3; i++) {
            int channelIndex = (i + startIndex) % 3;
            ColorChannel channel = allChannels[channelIndex];
            int colorComparisonResult = channel.compareColorValues(currentRgb, targetRgb, forRgb);
            if(colorComparisonResult == 0)
                continue;
            else
                return generateChannelHintWithColorName(colorComparisonResult < 0, channel.getChannelNameId(forRgb));
        }

        // the method did not return yet. all channels are correct
        return context.getString(R.string.hint_correct);
    }

    private String generateChannelHintWithColorName(boolean shouldIncrease, int colorNameId) {
        if(shouldIncrease)
            return context.getString(R.string.hint_increase, context.getString(colorNameId));
        else
            return context.getString(R.string.hint_decrease, context.getString(colorNameId));
    }
}
