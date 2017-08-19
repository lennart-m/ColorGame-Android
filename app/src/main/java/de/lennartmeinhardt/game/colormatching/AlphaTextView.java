package de.lennartmeinhardt.game.colormatching;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * A simple {@link android.widget.TextView} that correctly respects text alpha.
 */
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

    /**
     * Set the text color, respecting alpha.
     *
     * @param colors the color to set
     * @param alpha the alpha to set
     */
    private void setTextColorsWithAlpha(ColorStateList colors, int alpha) {
        super.setTextColor(colors.withAlpha(alpha));
    }

    /**
     * Set this view's alpha. This will set the text color so it has alpha.
     *
     * @param alpha the alpha to set
     */
    public void setAlpha(int alpha) {
        alpha &= 0xff;
        this.alpha = alpha;

        setTextColorsWithAlpha(getTextColors(), alpha);
    }
}
