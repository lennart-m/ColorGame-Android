package de.lennartmeinhardt.android.slider;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * A simple slider view for choosing an integer value between given minimum and maximum values.
 * This class is made abstract as it does not do any rendering; this has to be done by child classes.
 * This gives a lot of flexibility. Child classes may be based on {@link Drawable}s, like implemented in {@link android.widget.SeekBar}, or they may do some individual drawing, as {@link SimpleSlider} does.
 * Horizontal and vertical modes are supported, in the form of different {@link ValueDirection}s.
 * A step count can be given. Then the user can only set values that are rounded to nearest steps.
 */
@InverseBindingMethods(
        @InverseBindingMethod(type = Slider.class, attribute = "value")
)
public abstract class Slider extends View {

    private OnSliderValueChangedListener onSliderValueChangedListener;

    // default property values, as set by SeekBar
    private static final int
            DEF_MIN = 0,
            DEF_MAX = 100,
            DEF_VALUE = 0,
            // default value steps such that each value can be attained
            DEF_VALUE_STEPS = -1;
    // default value direction is horizontal, LTR or RTL depending on layout direction
    private static final ValueDirection
            DEF_VALUE_DIRECTION = ValueDirection.START_TO_END;

    // the current value
    private int value;
    // the min/max values, and the amount of steps
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

            int valueDirectionIndex = a.getInt(R.styleable.Slider_valueDirection, -1);
            ValueDirection valueDirection;
            try {
                valueDirection = ValueDirection.values()[valueDirectionIndex];
            } catch(ArrayIndexOutOfBoundsException e) {
                valueDirection = DEF_VALUE_DIRECTION;
            }
            setValueDirection(valueDirection);
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
                // left/right keys are handled only in horizontal mode
                if(isHorizontalOrientation()) {
                    if(shouldReverseInHorizontalMode())
                        // increase if left corresponds to higher values
                        newValue = getValueIncrementedBySteps(1);
                    else
                        // decrease if left corresponds to lower values
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
                // up/down are only handled in vertical mode
                if(isVerticalOrientation()) {
                    if(valueDirection == ValueDirection.TOP_TO_BOTTOM)
                        // increase if bottom corresponds to higher value
                        newValue = getValueIncrementedBySteps(1);
                    else
                        // decrease if bottom corresponds to higher value
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

        // mark this event as handled only if the value has changed
        if(value != newValue) {
            setValueInternal(newValue, false, true);
            return true;
        } else
            // if the value hasn't changed let super handle the event, this will take care of focus, for example
            return super.onKeyDown(keyCode, event);
    }

    /**
     * Calculate the new value if the current value will be incremented by the given amount of steps.
     * May be negative.
     *
     * @param stepCount the steps count to increment by
     * @return the new value to set after incrementing
     */
    private int getValueIncrementedBySteps(int stepCount) {
        int newValue = Math.round(value + stepCount * getStepSize());
        return trimValue(newValue);
    }

    public int getEffectiveValueSteps() {
        if(valueSteps > 0)
            return valueSteps;
        else
            return maxValue - minValue;
    }

    /**
     * Calculate the step size depending on range (max-min) and step count.
     *
     * @return the number of steps
     */
    private float getStepSize() {
        return (maxValue - minValue) / (float) getEffectiveValueSteps();
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
        // enable usage in a scrollview, else the scrollview will intercept touch events to detect scrolling
        if (getParent() != null)
            getParent().requestDisallowInterceptTouchEvent(true);
        setPressed(true);
        invalidate();
    }

    private void onStopTrackingTouch() {
        setPressed(false);
        invalidate();
    }

    /**
     * Handle this touch event; calculate and set the new value correponsing to the current touch position.
     *
     * @param event the touch event to track
     */
    private void trackTouchEvent(MotionEvent event) {
        float newRelativeValue;

        if(isHorizontalOrientation())
            newRelativeValue = touchPositionToRelativeValueHorizontal(event);
        else
            newRelativeValue = touchPositionToRelativeValueVertical(event);

        // trim the value
        newRelativeValue = Math.max(0, Math.min(1, newRelativeValue));

        int newValue = relativeToAbsoluteValue(newRelativeValue);
        setValueInternal(newValue, false, true);
    }

    /**
     * Given a relative value (between 0 and 1), calculate the absolute value (between min and max, rounded to steps).
     *
     * @param relativeValue value between 0 and 1, treated as relative value
     * @return the corresponding value between min and max, rounded to steps
     */
    private int relativeToAbsoluteValue(float relativeValue) {
        int nearestValueStep = Math.round(relativeValue * getEffectiveValueSteps());
        return Math.round(minValue + nearestValueStep * getStepSize());
    }

    /**
     * Calculate the relative value from a motion event in horizontal mode.
     * The relative value will be between 0 and 1.
     *
     * @param event the event to convert to relative value
     * @return relative value from the x part of the event
     */
    private float touchPositionToRelativeValueHorizontal(MotionEvent event) {
        final float x = event.getX();

        float newRelativeValue = positionToRelativeValue(x - getPaddingLeft(), getPaddedWidth());

        if(shouldReverseInHorizontalMode())
            newRelativeValue = 1 - newRelativeValue;

        return newRelativeValue;
    }

    /**
     * Calculate the relative value from a motion event in vertical mode.
     * The relative value will be between 0 and 1.
     *
     * @param event the event to convert to relative value
     * @return relative value from the y part of the event
     */
    private float touchPositionToRelativeValueVertical(MotionEvent event) {
        final float y = event.getY();

        float newRelativeValue = positionToRelativeValue(y - getPaddingTop(), getPaddedHeight());

        if(valueDirection == ValueDirection.BOTTOM_TO_TOP)
            newRelativeValue = 1 - newRelativeValue;

        return newRelativeValue;
    }

    /**
     * Check if the value direction is such that the min value is on the right.
     *
     * @return true if the min value is on the right, false if it's on the left
     */
    protected boolean shouldReverseInHorizontalMode() {
        boolean isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        return valueDirection == ValueDirection.RIGHT_TO_LEFT
                || (isRTL && valueDirection == ValueDirection.START_TO_END)
                || (! isRTL && valueDirection == ValueDirection.END_TO_START);
    }

    /**
     * Check if this {@link Slider} is in horizontal mode, i.e. has {@link ValueDirection} LEFT_TO_RIGHT, RIGHT_TO_LEFT, START_TO_END, or END_TO_START.
     *
     * @return true if the slider has any horizontal {@link ValueDirection}
     */
    public boolean isHorizontalOrientation() {
        return valueDirection.isHorizontal();
    }

    /**
     * Check if this {@link Slider} is in vertical mode, i.e. has {@link ValueDirection} BOTTOM_TO_TOP or TOP_TO_BOTTOM.
     *
     * @return true if the slider has any vertical {@link ValueDirection}
     */
    public boolean isVerticalOrientation() {
        return valueDirection.isVertical();
    }

    /**
     * Trim a value so it's between min and max.
     *
     * @param value the value to trim
     * @return the corresponding value between min and max
     */
    private int trimValue(int value) {
        return value < minValue ? minValue : value > maxValue ? maxValue : value;
    }

    /**
     * Get the available width, i.e. the total width without padding.
     *
     * @param totalWidth the total width
     * @return the available width
     */
    protected int getPaddedWidth(int totalWidth) {
        return totalWidth - getPaddingLeft() - getPaddingRight();
    }
    /**
     * Get the available width, i.e. this view's total width without padding.
     *
     * @return the available width
     */
    protected int getPaddedWidth() {
        return getPaddedWidth(getWidth());
    }

    /**
     * Get the available height, i.e. the total heightwithout padding.
     *
     * @param totalHeight the total height
     * @return the available height
     */
    protected int getPaddedHeight(int totalHeight) {
        return totalHeight - getPaddingTop() - getPaddingBottom();
    }
    /**
     * Get the available height, i.e. this view's total height without padding.
     *
     * @return the available height
     */
    protected int getPaddedHeight() {
        return getPaddedHeight(getHeight());
    }


    /**
     * Get the relative value, e.g. 0 if value is min, 1 if it's max.
     *
     * @return the relative position of the current value between min and max
     */
    protected float getRelativeValue() {
        if (minValue >= maxValue)
            return 0;
        else
            return (value - minValue) / (float) (maxValue - minValue);
    }

    /**
     * Get the relative value from the position in the size. Padding has been taken into account already.
     *
     * @param positionInPaddedSize the position in the view, without padding
     * @param paddedSize the available size
     * @return the relative value corresponding to the given position
     */
    protected float positionToRelativeValue(float positionInPaddedSize, int paddedSize) {
        int availableSize = touchableTrackSize(paddedSize);
        int offset = (paddedSize - availableSize) / 2;
        if(availableSize > 0)
            return (positionInPaddedSize - offset) / availableSize;
        else
            return 0;
    }

    /**
     * Get the size of the touchable area of the track. Padding already has been considered.
     * This might return a value lower than padding, for example if the thumb's size is to be subtracted from the padded size.
     *
     * @param paddedSize the view's size, without padding
     * @return the size of the touchable track area
     */
    protected int touchableTrackSize(int paddedSize) {
        return paddedSize;
    }

    /**
     * Get the current value.
     *
     * @return the current value
     */
    public int getValue() {
        return value;
    }

    /**
     * Set this slider's value.
     *
     * @param value the new value to set
     */
    public void setValue(int value) {
        setValueInternal(value, false, false);
    }

    /**
     * Set the slider's value. The listener is informed about the change if the new value differs from the old one, of if forceUpdate is true.
     *
     * @param value the new value to set
     * @param forceUpdate force informing the listener, ignoring if the value actually changed
     * @param fromUser true if the user initiated the value change
     */
    private void setValueInternal(int value, boolean forceUpdate, boolean fromUser) {
        if (this.value != value || forceUpdate) {
            this.value = value;
            if (onSliderValueChangedListener != null)
                onSliderValueChangedListener.onSliderValueChanged(this, value, fromUser);
            invalidate();
        }
    }

    /**
     * Set a listener to inform about value changes.
     * Put null in order to remove the currently set listener.
     *
     * @param onSliderValueChangedListener the value listener to set
     */
    public void setOnSliderValueChangedListener(@Nullable OnSliderValueChangedListener onSliderValueChangedListener) {
        this.onSliderValueChangedListener = onSliderValueChangedListener;
    }

    /**
     * Get the minimum value that the user can set.
     *
     * @return the minimum user settable value
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * Set the minimum value that the user can set.
     *
     * @param minValue the new minimum user settable value
     */
    public void setMinValue(int minValue) {
        this.minValue = minValue;
        invalidate();
    }

    /**
     * Get the maximum value that the user can set.
     *
     * @return the maximum user settable value
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Set the maximum value that the user can set.
     *
     * @param maxValue the new maximum user settable value
     */
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

    /**
     * Get the number of value steps.
     *
     * @return the number of value steps the user can set
     */
    public int getValueSteps() {
        return valueSteps;
    }

    /**
     * Set the number of value steps.
     * Set -1 in order to have a step size of 1, independent of min and max.
     *
     * @param valueSteps the new number of value steps the user can set
     */
    public void setValueSteps(int valueSteps) {
        this.valueSteps = valueSteps;
    }

    /**
     * Set the {@link ValueDirection} to use.
     *
     * @param valueDirection the new {@link ValueDirection} to use
     */
    public void setValueDirection(@NonNull ValueDirection valueDirection) {
        if(this.valueDirection != valueDirection) {
            this.valueDirection = valueDirection;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Get this {@link Slider}'s {@link ValueDirection}.
     *
     * @return the {@link ValueDirection}
     */
    public ValueDirection getValueDirection() {
        return valueDirection;
    }

    /**
     * The {@link ValueDirection} determines if the slider is in horizontal or vertical mode, and if its min and max are on the left, right, bottom, or top.
     * Also touch and keyboard events are handled differently, depending on the {@link ValueDirection}.
     */
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


    /**
     * Used for bi-directional binding of a {@link Slider}'s value.
     *
     * @param slider the slider whose value is to be bound
     * @param bindingListener the binding listener to be informed about value changes
     */
    @BindingAdapter("valueAttrChanged")
    public static void setListener(Slider slider, final InverseBindingListener bindingListener) {
        // if no listener is to set, remove the current listener
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


    /**
     * A listener that can be informed about a {@link Slider}'s value change events.
     */
    public interface OnSliderValueChangedListener {

        /**
         * Called when a {@link Slider}'s value is changed.
         *
         * @param slider the {@link Slider} whose value has changed
         * @param value the new {@link Slider} value
         * @param fromUser true if the user initiated the change (by touch or by keyboard)
         */
        void onSliderValueChanged(Slider slider, int value, boolean fromUser);
    }
}