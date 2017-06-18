package de.lennartmeinhardt.game.colormatching;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class RectangularChildrenLayout extends ViewGroup {

    public RectangularChildrenLayout(@NonNull Context context) {
        super(context);
    }

    public RectangularChildrenLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangularChildrenLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int maxWidth = right - left,
            maxHeight = bottom - top,
            size = Math.min(maxWidth, maxHeight),
            x = (maxWidth - size) / 2,
            y = (maxHeight - size) / 2;
        int childCount = getChildCount();
        for(int i = 0; i < childCount; i++) {
            getChildAt(i).layout(x, y, x + size, y + size);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }
}
