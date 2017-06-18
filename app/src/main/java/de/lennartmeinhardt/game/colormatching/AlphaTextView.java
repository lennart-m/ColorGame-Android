package de.lennartmeinhardt.game.colormatching;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

// TODO: check if v26 AppCompatTextView correctly sets alpha
public class AlphaTextView extends AppCompatTextView {

    private int alpha;


    public AlphaTextView(Context context) {
        super(context);
    }

    public AlphaTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        setTextColorsWithAlpha(colors, alpha);
    }

    @Override
    public void setTextColor(@ColorInt int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    private void setTextColorsWithAlpha(ColorStateList colors, int alpha) {
        super.setTextColor(colors.withAlpha(alpha));
    }

    public void setAlpha(int alpha) {
        alpha &= 0xff;
        this.alpha = alpha;

        setTextColorsWithAlpha(getTextColors(), alpha);
    }
}
