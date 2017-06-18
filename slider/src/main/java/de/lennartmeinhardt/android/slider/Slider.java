package de.lennartmeinhardt.android.slider;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

@InverseBindingMethods(
        @InverseBindingMethod(type = Slider.class, attribute = "value")
)
public abstract class Slider extends View {

    private OnSliderValueChangedListener onSliderValueChangedListener;

    private static final int
            DEF_MIN = 0,
            DEF_MAX = 100,
            DEF_VALUE = 0,
            DEF_VALUE_STEPS = DEF_MAX;
    private static final ValueDirection
            DEF_VALUE_DIRECTION = ValueDirection.START_TO_END;


    private int value;
    private int minValue, maxValue, valueSteps;
    private ValueDirection valueDirection;


    public Slider(Context context) {
        this(context, null);
    }

    public Slider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Slider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setFocusable(true);

        if(attrs != null)
            readFromAttributes(context, attrs);
        else
            setDefaultValues();
    }


    private void readFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.Slider);
        try {
            setMinValue(a.getInt(R.styleable.Slider_minValue, DEF_MIN));
            setMaxValue(a.getInt(R.styleable.Slider_maxValue, DEF_MAX));
            setValueSteps(a.getInt(R.styleable.Slider_valueSteps, DEF_VALUE_STEPS));
            setValueInternal(a.getInt(R.styleable.Slider_value, DEF_VALUE), true, false);

            int valueDirectionIndex = a.getInt(R.styleable.Slider_valueDirection, DEF_VALUE_DIRECTION.ordinal());
            setValueDirection(ValueDirection.values()[valueDirectionIndex]);
        } finally {
            a.recycle();
        }
    }

    private void setDefaultValues() {
        setMinValue(DEF_MIN);
        setMaxValue(DEF_MAX);
        setValueSteps(DEF_VALUE_STEPS);
        setValueInternal(DEF_VALUE, true, false);
        setValueDirection(DEF_VALUE_DIRECTION);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(! isEnabled())
            return false;

        int newValue = value;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(isHorizontalOrientation()) {
                    if(shouldReverseInHorizontalMode())
                        newValue = getValueIncrementedBySteps(1);
                    else
                        newValue = getValueIncrementedBySteps(-1);
                }
                break;
            case KeyEvent.KEYCODE_MINUS:
                newValue = getValueIncrementedBySteps(-1);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(isHorizontalOrientation()) {
                    if(shouldReverseInHorizontalMode())
                        newValue = getValueIncrementedBySteps(-1);
                    else
                        newValue = getValueIncrementedBySteps(1);
                }
                break;
            case KeyEvent.KEYCODE_PLUS:
                newValue = getValueIncrementedBySteps(1);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(isVerticalOrientation()) {
                    if(valueDirection == ValueDirection.TOP_TO_BOTTOM)
                        newValue = getValueIncrementedBySteps(1);
                    else
                        newValue = getValueIncrementedBySteps(-1);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if(isVerticalOrientation()) {
                    if(valueDirection == ValueDirection.BOTTOM_TO_TOP)
                        newValue = getValueIncrementedBySteps(1);
                    else
                        newValue = getValueIncrementedBySteps(-1);
                }
                break;
        }

        if(value != newValue) {
            setValueInternal(newValue, false, true);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private int getValueIncrementedBySteps(int stepCount) {
        int newValue = Math.round(value + stepCount * getStepSize());
        return trimValue(newValue);
    }

    private float getStepSize() {
        return (maxValue - minValue) / (float) valueSteps;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(! isEnabled())
            return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onStartTrackingTouch();
                // fallthrough
            case MotionEvent.ACTION_MOVE:
                trackTouchEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
                trackTouchEvent(event);
                // fallthrough
            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void onStartTrackingTouch() {
        // enable usage in a scrollview, for example
        if (getParent() != null)
            getParent().requestDisallowInterceptTouchEvent(true);
        setPressed(true);
        invalidate();
    }

    private void onStopTrackingTouch() {
        setPressed(false);
        invalidate();
    }

    private void trackTouchEvent(MotionEvent event) {
        float newRelativeValue;

        if(isHorizontalOrientation())
            newRelativeValue = touchPositionToRelativeValueHorizontal(event);
        else
            newRelativeValue = touchPositionToRelativeValueVertical(event);

        // trim the value
        newRelativeValue = Math.max(0, Math.min(1, newRelativeValue));

//        setHotspot(x, getHeight() / 2);
        int newValue = relativeToAbsoluteValue(newRelativeValue);
        setValueInternal(newValue, false, true);
    }

    private int relativeToAbsoluteValue(float relativeValue) {
        int nearestValueStep = Math.round(relativeValue * valueSteps);
        return Math.round(minValue + nearestValueStep * getStepSize());
    }

    private float touchPositionToRelativeValueHorizontal(MotionEvent event) {
        final float x = event.getX();

        float newRelativeValue = positionToRelativeValue(x - getPaddingLeft(), getPaddedWidth());

        if(shouldReverseInHorizontalMode())
            newRelativeValue = 1 - newRelativeValue;

        return newRelativeValue;
    }

    private float touchPositionToRelativeValueVertical(MotionEvent event) {
        final float y = event.getY();

        float newRelativeValue = positionToRelativeValue(y - getPaddingTop(), getPaddedHeight());

        if(valueDirection == ValueDirection.BOTTOM_TO_TOP)
            newRelativeValue = 1 - newRelativeValue;

        return newRelativeValue;
    }

    protected boolean shouldReverseInHorizontalMode() {
        boolean isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        return valueDirection == ValueDirection.RIGHT_TO_LEFT
                || (isRTL && valueDirection == ValueDirection.START_TO_END)
                || (! isRTL && valueDirection == ValueDirection.END_TO_START);
    }

    public boolean isHorizontalOrientation() {
        return valueDirection.isHorizontal();
    }

    public boolean isVerticalOrientation() {
        return valueDirection.isVertical();
    }

    private void setHotspot(float x, float y) {
        final Drawable bg = getBackground();
        if (bg != null) {
            DrawableCompat.setHotspot(bg, x, y);
        }
    }

    private int trimValue(int value) {
        return value < minValue ? minValue : value > maxValue ? maxValue : value;
    }

    protected int getPaddedWidth(int totalWidth) {
        return totalWidth - getPaddingLeft() - getPaddingRight();
    }
    protected int getPaddedWidth() {
        return getPaddedWidth(getWidth());
    }

    protected int getPaddedHeight(int totalHeight) {
        return totalHeight - getPaddingTop() - getPaddingBottom();
    }
    protected int getPaddedHeight() {
        return getPaddedHeight(getHeight());
    }


    protected float getRelativeValue() {
        if (getMinValue() >= getMaxValue())
            return 0;
        else
            return getValue() / (float) (getMaxValue() - getMinValue());
    }

    protected float positionToRelativeValue(float positionInPaddedSize, int paddedSize) {
        int availableSize = touchableTrackSize(paddedSize);
        int offset = (paddedSize - availableSize) / 2;
        if(availableSize > 0)
            return (positionInPaddedSize - offset) / availableSize;
        else
            return 0;
    }

    protected int touchableTrackSize(int paddedSize) {
        return paddedSize;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        setValueInternal(value, false, false);
    }

    private void setValueInternal(int value, boolean forceUpdate, boolean fromUser) {
        if (this.value != value || forceUpdate) {
            this.value = value;
            if (onSliderValueChangedListener != null)
                onSliderValueChangedListener.onSliderValueChanged(this, value, fromUser);
            invalidate();
        }
    }

    public void setOnSliderValueChangedListener(OnSliderValueChangedListener onSliderValueChangedListener) {
        this.onSliderValueChangedListener = onSliderValueChangedListener;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        invalidate();
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setPressed(isPressed() && enabled);
        invalidate();
    }

    public int getValueSteps() {
        return valueSteps;
    }

    public void setValueSteps(int valueSteps) {
        this.valueSteps = valueSteps;
    }

    public void setValueDirection(ValueDirection valueDirection) {
        if(this.valueDirection != valueDirection) {
            this.valueDirection = valueDirection;
            requestLayout();
            invalidate();
        }
    }

    public ValueDirection getValueDirection() {
        return valueDirection;
    }

    public enum ValueDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        START_TO_END,
        END_TO_START,
        BOTTOM_TO_TOP,
        TOP_TO_BOTTOM;

        public boolean isHorizontal() {
            return ! isVertical();
        }
        public boolean isVertical() {
            return this == TOP_TO_BOTTOM || this == BOTTOM_TO_TOP;
        }
    }


    public interface OnSliderValueChangedListener {
        void onSliderValueChanged(Slider slider, int value, boolean fromUser);
    }

    @BindingAdapter("valueAttrChanged")
    public static void setListener(Slider slider, final InverseBindingListener bindingListener) {
        if (bindingListener == null)
            slider.setOnSliderValueChangedListener(null);
        else
            slider.setOnSliderValueChangedListener(new OnSliderValueChangedListener() {
                @Override
                public void onSliderValueChanged(Slider slider, int value, boolean fromUser) {
                    bindingListener.onChange();
                }
            });
    }
}