package de.lennartmeinhardt.android.slider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

public class SimpleSlider extends Slider {

    private static final int DEF_TINT = Color.WHITE;
    // the relative size of the area around the thumb being cleared
    private static final float THUMB_OUTER_CLEAR_FACTOR = 1.5f;
    // the relative size of the area inside the thumb being cleared (while tracking touch or focused)
    private static final float THUMB_INNER_CLEAR_FACTOR = .5f;

    public static final int ALPHA_FOCUSED_PRESSED = 255;
    public static final int ALPHA_DISABLED = 100;
    public static final int ALPHA_NORMAL = 200;


    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int thumbRadius;
    private int trackThickness;


    public SimpleSlider(Context context) {
        this(context, null);
    }

    public SimpleSlider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(attrs != null)
            readFromAttributes(context, attrs);
        else
            setDefaultValues(context);

        initializePaints();
    }

    private void initializePaints() {
        thumbPaint.setStyle(Paint.Style.FILL);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setStyle(Paint.Style.FILL);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // need software rendering so cleaning the canvas does not draw in black
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
    }


    private void readFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.SimpleSlider);
        try {
            setTint(a.getColor(R.styleable.SimpleSlider_tint, DEF_TINT));

            final int defThumbRadius = context.getResources().getDimensionPixelSize(R.dimen.simple_slider_default_thumb_radius);
            setThumbRadius(a.getDimensionPixelSize(R.styleable.SimpleSlider_thumbRadius, defThumbRadius));

            final int defTrackThickness = context.getResources().getDimensionPixelSize(R.dimen.simple_slider_default_track_thickness);
            setTrackThickness(a.getDimensionPixelSize(R.styleable.SimpleSlider_trackThickness, defTrackThickness));
        } finally {
            a.recycle();
        }
    }

    private void setDefaultValues(Context context) {
        setTint(DEF_TINT);
        int defThumbRadius = context.getResources().getDimensionPixelSize(R.dimen.simple_slider_default_thumb_radius);
        setThumbRadius(defThumbRadius);
        int defTrackThickness = context.getResources().getDimensionPixelSize(R.dimen.simple_slider_default_track_thickness);
        setTrackThickness(defTrackThickness);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int thumbDiameter = 2 * thumbRadius;
        int preferredWidth, preferredHeight;

        if(getValueDirection().isHorizontal()) {
            preferredWidth = 0;
            preferredHeight = Math.max(thumbDiameter, trackThickness);
        } else {
            preferredWidth = Math.max(thumbDiameter, trackThickness);
            preferredHeight = 0;
        }

        preferredWidth += getPaddingLeft() + getPaddingRight();
        preferredHeight += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(
                ViewCompat.resolveSizeAndState(preferredWidth, widthMeasureSpec, 0),
                ViewCompat.resolveSizeAndState(preferredHeight, heightMeasureSpec, 0)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // determine drawing alpha
        updateDrawingAlpha();

        // draw track
        canvas.drawLine(getTrackStartX(), getTrackStartY(), getTrackEndX(), getTrackEndY(), trackPaint);

        final float thumbCenterX = getThumbCenterX(),
                    thumbCenterY = getThumbCenterY();
        // clear area around the thumb (software rendering needed for this)
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius * THUMB_OUTER_CLEAR_FACTOR, clearPaint);
        // draw the actual thumb
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius, thumbPaint);

        // clear inside the thumb if user is interacting with this slider
        if(isFocused() || isPressed())
            canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius * THUMB_INNER_CLEAR_FACTOR, clearPaint);
    }

    private void updateDrawingAlpha() {
        int alpha;
        if(! isEnabled())
            alpha = ALPHA_DISABLED;
        else if(isFocused() || isPressed())
            alpha = ALPHA_FOCUSED_PRESSED;
        else
            alpha = ALPHA_NORMAL;

        thumbPaint.setAlpha(alpha);
        trackPaint.setAlpha(alpha);
    }

    private float getThumbCenterX() {
        if(isHorizontalOrientation()) {
            float startPadding;
            float availableWidth = touchableTrackSize(getPaddedWidth());
            float thumbStartOffset = getRelativeValue() * availableWidth;
            if(shouldReverseInHorizontalMode()) {
                startPadding = getWidth() - getPaddingRight() - thumbRadius;
                thumbStartOffset = - thumbStartOffset;
            } else
                startPadding = getPaddingLeft() + thumbRadius;

            return startPadding + thumbStartOffset;
        } else {
            // in vertical mode thumb x is equal to track x
            return getTrackStartX();
        }
    }

    private float getThumbCenterY() {
        if(isHorizontalOrientation()) {
            // in horizontal mode thumb y is equal to track y
            return getTrackStartY();
        } else {
            float startPadding;
            float availableHeight = touchableTrackSize(getPaddedHeight());
            float thumbStartOffset = getRelativeValue() * availableHeight;
            if(getValueDirection() == ValueDirection.BOTTOM_TO_TOP) {
                startPadding = getHeight() - getPaddingBottom() - thumbRadius;
                thumbStartOffset = - thumbStartOffset;
            } else
                startPadding = getPaddingTop() + thumbRadius;

            return startPadding + thumbStartOffset;
        }
    }

    private float getTrackStartX() {
        if(isHorizontalOrientation())
            return getPaddingLeft() + thumbRadius;
        else
            // in vertical mode the track is horizontally centered in the available width
            return getPaddingLeft() + getPaddedWidth() / 2;
    }

    private float getTrackEndX() {
        if(isHorizontalOrientation())
            return getWidth() - getPaddingRight() - thumbRadius;
        else
            // in vertical mode track x is same at start and end
            return getTrackStartX();
    }

    private float getTrackStartY() {
        if(isHorizontalOrientation())
            // in horizontal mode the track is vertically centered in the available height
            return getPaddingTop() + getPaddedHeight() / 2f;
        else
            return getPaddingTop() + thumbRadius;
    }

    private float getTrackEndY() {
        if(isHorizontalOrientation())
            // in horizontal mode track y is same at start and end
            return getTrackStartY();
        else
            return getHeight() - getPaddingBottom() - thumbRadius;
    }

    public void setThumbRadius(int thumbRadius) {
        this.thumbRadius = thumbRadius;

        requestLayout();
        invalidate();
    }

    public int getThumbRadius() {
        return thumbRadius;
    }

    public void setTrackThickness(int trackThickness) {
        this.trackThickness = trackThickness;
        trackPaint.setStrokeWidth(trackThickness);

        requestLayout();
        invalidate();
    }
    public int getTrackThickness() {
        return trackThickness;
    }

    @Override
    protected int touchableTrackSize(int paddedSize) {
        return paddedSize - thumbRadius * 2;
    }

    public void setTint(int tintColor) {
        thumbPaint.setColor(tintColor);
        trackPaint.setColor(tintColor);
    }

    public int geTint() {
        return thumbPaint.getColor();
    }
}
