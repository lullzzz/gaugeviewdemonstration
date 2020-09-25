package lullzzzz.gshubina.testapp.client.view;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import lullzzzz.gshubina.testapp.R;


public class GaugeView extends View {

    private final float SCALE_ARC_ANGLE = 260.0f;
    private final int MIN_VALUE_ANGLE = -40;
    private final int MAX_VALUE_ANGLE = 220;
    private final float MIN_TEXT_STEP_ANGLE = 26.0f;

    private final int MAX_TEXT_STEP_COUNT = (int) (SCALE_ARC_ANGLE / MIN_TEXT_STEP_ANGLE);
    private final double MIN_VALUE = 0.0;

    private final int VALUE_ANIMATION_DURATION = 300;

    private final int TEXT_TICK_FACTOR = 15;

    private int mArrowColor;
    private int mTextColor;
    private int mTickColor;
    private String mUnitString;

    private float mMaxValue = 0;
    private double mCurrentValue = 0;

    private RectF mGaugeContainer;
    private RectF mValueTextContainer;

    private float mTextTickStep;
    private long mTextTickCount;
    private long mTickCount;
    private float mScaleFactor = 1f;
    private String mScaleFactorText;

    private Bitmap mCashedBitmap;
    private Canvas mCashedCanvas;

    private Paint mTicksPaint;
    private Paint mRimPaint;
    private Paint mArrowPaint;
    private Paint mTextPaint;
    private Paint mBackground1Paint;
    private Paint mBackground2Paint;

    private float[] mTextMarkPointArray;
    private float[] mMarkPointArray;
    private float mTextMarkStepAngle;
    private final ArrayList<Label> mMarkLabelArray = new ArrayList<>();

    private int[] mBackground1GradientColors;
    private int[] mBackground2GradientColors;

    private final ValueAnimator mValueAnimator = ValueAnimator.ofObject(
            (TypeEvaluator<Double>) (fraction, startValue, endValue) -> (startValue + (endValue - startValue) * fraction),
            new Double(0), new Double(0));

    public GaugeView(Context context) {
        super(context);
        init(null, 0);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GaugeView, defStyle, 0);

        mMaxValue = a.getInt(
                R.styleable.GaugeView_maxValue, 0);

        mTickColor = a.getColor(R.styleable.GaugeView_tickColor,
                getResources().getColor(R.color.default_tick_color, getContext().getTheme()));
        mArrowColor = a.getColor(R.styleable.GaugeView_arrowColor,
                getResources().getColor(R.color.default_arrow_color, getContext().getTheme()));
        mTextColor = a.getColor(R.styleable.GaugeView_textColor,
                getResources().getColor(R.color.default_text_color, getContext().getTheme()));

        mUnitString = a.getString(R.styleable.GaugeView_unit);

        a.recycle();

        mValueAnimator.setDuration(VALUE_ANIMATION_DURATION);
        mValueAnimator.addUpdateListener(updatedAnimation -> {
            Double animatedValue = (Double) updatedAnimation.getAnimatedValue();
            setCurrentValueUi(animatedValue);
        });

        calculateTickArrays();

        mTicksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTicksPaint.setStyle(Paint.Style.STROKE);
        mTicksPaint.setColor(mTickColor);

        mRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRimPaint.setStyle(Paint.Style.STROKE);
        mRimPaint.setColor(mTickColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setLinearText(true);

        mBackground1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackground1Paint.setStyle(Paint.Style.FILL);
        mBackground1Paint.setColor(Color.BLACK);

        mBackground2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackground2Paint.setStyle(Paint.Style.FILL);
        mBackground2Paint.setColor(Color.WHITE);

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStyle(Paint.Style.FILL);
        mArrowPaint.setColor(mArrowColor);
        mArrowPaint.setStrokeCap(Paint.Cap.ROUND);

        mBackground1GradientColors = new int[]{
                getResources().getColor(R.color.background1_gradient_start_color, getContext().getTheme()),
                getResources().getColor(R.color.background1_gradient_center_color, getContext().getTheme()),
                getResources().getColor(R.color.background1_gradient_end_color, getContext().getTheme())
        };

        mBackground2GradientColors = new int[]{
                getResources().getColor(R.color.background2_gradient_start_color, getContext().getTheme()),
                getResources().getColor(R.color.background2_gradient_center_color, getContext().getTheme()),
                getResources().getColor(R.color.background2_gradient_center1_color, getContext().getTheme()),
                getResources().getColor(R.color.background2_gradient_end_color, getContext().getTheme()),
                getResources().getColor(R.color.background2_gradient_center1_color, getContext().getTheme()),
                getResources().getColor(R.color.background2_gradient_center_color, getContext().getTheme()),
                getResources().getColor(R.color.background2_gradient_start_color, getContext().getTheme())
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int paddingStart = getPaddingStart() == 0 ? getPaddingLeft() : getPaddingStart();
        int paddingEnd = getPaddingEnd() == 0 ? getPaddingRight() : getPaddingEnd();

        mGaugeContainer = new RectF(paddingStart, getPaddingTop(),
                getWidth() - paddingEnd, getHeight() - getPaddingBottom());

        float dim = Math.min(mGaugeContainer.height(), mGaugeContainer.width());
        float diffW = mGaugeContainer.width() - dim;
        float diffH = mGaugeContainer.height() - dim;

        mGaugeContainer.left += diffW / 2f;
        mGaugeContainer.right -= diffW / 2f;
        mGaugeContainer.top += diffH / 2f;
        mGaugeContainer.bottom -= diffH / 2f;

        if (mCashedBitmap != null) {
            mCashedBitmap.recycle();
        }
        mCashedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCashedCanvas = new Canvas(mCashedBitmap);

        mValueTextContainer = scale(mGaugeContainer, 0.87f);

        mTextPaint.setTextSize(mGaugeContainer.width() / TEXT_TICK_FACTOR);
        mArrowPaint.setStrokeWidth(mGaugeContainer.width() / (5 * TEXT_TICK_FACTOR));
        mTicksPaint.setStrokeWidth(mGaugeContainer.width() / (10 * TEXT_TICK_FACTOR));
        mRimPaint.setStrokeWidth(mGaugeContainer.width() / (10 * TEXT_TICK_FACTOR));

        drawBackground(mCashedCanvas);
        createTickPathArray();
        drawTicks(mCashedCanvas);

        if (mUnitString != null) {
            mCashedCanvas.drawText(mUnitString,
                    mValueTextContainer.centerX(), mValueTextContainer.centerY() + mValueTextContainer.height() / 3,
                    mTextPaint);
        }

        if (mScaleFactorText != null) {
            mCashedCanvas.drawText("x" + mScaleFactorText,
                    mValueTextContainer.centerX(), mValueTextContainer.centerY() - mValueTextContainer.height() / 6,
                    mTextPaint);
        }

        mCashedBitmap.prepareToDraw();
    }

    private void drawBackground(Canvas cashCanvas) {
        mBackground2Paint.setColor(Color.BLACK);
        cashCanvas.drawArc(mGaugeContainer, 0, 360, true, mBackground2Paint);

        LinearGradient background1Gradient = new LinearGradient(
                mGaugeContainer.left, mGaugeContainer.top,
                mGaugeContainer.right, mGaugeContainer.bottom,
                mBackground1GradientColors, null, Shader.TileMode.MIRROR);
        mBackground1Paint.setShader(background1Gradient);
        cashCanvas.drawArc(scale(mGaugeContainer, 0.99f), 0, 360, true, mBackground1Paint);

        mBackground2Paint.setColor(Color.WHITE);
        cashCanvas.drawArc(scale(mGaugeContainer, 0.97f), 0, 360, true, mBackground2Paint);

        mBackground2Paint.setColor(Color.BLACK);
        cashCanvas.drawArc(scale(mGaugeContainer, 0.96f), 0, 360, true, mBackground2Paint);

        cashCanvas.drawArc(scale(mGaugeContainer, 0.95f), 0, 360, true, mBackground1Paint);

        mBackground2Paint.setColor(Color.BLACK);
        cashCanvas.drawArc(scale(mGaugeContainer, 0.93f), 0, 360, true, mBackground2Paint);

        RectF background6Rect = scale(mGaugeContainer, 0.92f);
        SweepGradient background2Gradient = new SweepGradient(
                background6Rect.centerX(), background6Rect.centerY(),
                mBackground2GradientColors, null);
        mBackground1Paint.setShader(background2Gradient);
        cashCanvas.drawArc(background6Rect, 0, 360, true, mBackground1Paint);

        mBackground2Paint.setColor(Color.BLACK);
        cashCanvas.drawArc(scale(mGaugeContainer, 0.15f), 0, 360, true, mBackground2Paint);

        mBackground2Paint.setColor(Color.GRAY);
        cashCanvas.drawArc(scale(mGaugeContainer, 0.14f), 0, 360, true, mBackground2Paint);

        cashCanvas.drawArc(scale(mGaugeContainer, 0.12f), 0, 360, true, mBackground1Paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCashedBitmap, 0f, 0f, null);
        drawArrow(canvas);
    }

    private void calculateTickArrays() {
        /* How many units could be in one step */
        float unitsPerTickStep = mMaxValue / MAX_TEXT_STEP_COUNT;

        if (unitsPerTickStep == 1) { // case when we have max value equals MAX_TEXT_STEP_COUNT
            mTextTickCount = MAX_TEXT_STEP_COUNT + 1; // plus zero tick
            mTickCount = MAX_TEXT_STEP_COUNT; // small marks (without text)
            mTextTickStep = 1;
            mTextMarkStepAngle = MIN_TEXT_STEP_ANGLE;
        } else {
            if (unitsPerTickStep > 1) {
                int factor = unitsPerTickStep < 5 ? 2 : 5; //one tick step will be a multiple of 2 or 5 units
                // units per factor step
                double y = unitsPerTickStep / factor;
                y = Math.ceil(y);
                mTextTickStep = Math.round(y * factor);
                mTextTickCount = (long) (Math.ceil(mMaxValue / mTextTickStep));
                mTickCount = mTextTickCount;
                // new max value if need
                mMaxValue = mTextTickCount * mTextTickStep;
                mTextMarkStepAngle = SCALE_ARC_ANGLE / mTextTickCount;
                ++mTextTickCount; // plus zero tick
                //scale factor
                if (y > 10) {
                    mScaleFactor = calculateScaleFactor(mMaxValue);
                    mScaleFactorText = String.valueOf((int) mScaleFactor);
                    mMaxValue = mMaxValue / mScaleFactor;
                    mTextTickStep = mTextTickStep / mScaleFactor;
                }
            } else {
                mScaleFactor = calculateNegativeScaleFactor(mMaxValue, mScaleFactor);
                float scale = 1 / mScaleFactor;
                mScaleFactorText = scale > 1 ? "10^-" + (int) scale / 10 : null;
                float factor1 = unitsPerTickStep < 0.5f ? 0.2f : 0.5f; //one tick step will be a multiple of 0.2 or 0.5 units
                // units per factor step
                float y = unitsPerTickStep / factor1;
                mTextTickStep = (int) Math.ceil(y) * factor1;
                mTextTickCount = (long) (Math.ceil(mMaxValue / mTextTickStep));
                mTickCount = mTextTickCount;
                mMaxValue = mMaxValue / mScaleFactor;
                // new max value if need
                mMaxValue = mTextTickCount * mTextTickStep;
                mTextMarkStepAngle = SCALE_ARC_ANGLE / mTextTickCount;
                ++mTextTickCount; // plus zero tick
            }
        }
    }

    private float calculateScaleFactor(double value) {
        if (value < 100) {
            return 1;
        } else {
            mScaleFactor *= 10;
            value = value / mScaleFactor;
            return mScaleFactor * calculateScaleFactor(value);
        }
    }

    private float calculateNegativeScaleFactor(double value, float factor) {
        if (value == 0 || value >= 1) {
            return factor;
        } else {
            factor /= 10.0f;
            value = value * 10.0f;
            return calculateNegativeScaleFactor(value, factor);
        }
    }

    private void drawTicks(Canvas canvas) {
        canvas.drawLines(mTextMarkPointArray, mTicksPaint);
        canvas.drawLines(mMarkPointArray, mTicksPaint);
        for (Label valueLabel : mMarkLabelArray) {
            canvas.drawText(valueLabel.getText(), valueLabel.getX(), valueLabel.getY(), mTextPaint);
        }
        canvas.drawArc(mValueTextContainer, (-1 * MIN_VALUE_ANGLE), (-1 * SCALE_ARC_ANGLE), false, mRimPaint);

    }

    private void drawArrow(Canvas canvas) {
        float arrowRadius = mValueTextContainer.width() * 0.48f;
        RectF shank = scale(mValueTextContainer, 0.03f);

        float angle = MIN_VALUE_ANGLE + (float) (getCurrentValue() / mMaxValue * 260);
        canvas.drawLine(
                (float) (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI)),
                (float) (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI)),
                (float) (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (arrowRadius)),
                (float) (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI) * (arrowRadius)),
                mArrowPaint
        );
        canvas.drawArc(shank, 0, 360, true, mArrowPaint);
    }

    private void createTickPathArray() {
        float minorStep = mTextMarkStepAngle / 2;

        float textTicksLength = mGaugeContainer.width() / TEXT_TICK_FACTOR;
        float ticksLength = textTicksLength / 2;

        float currentAngle = MIN_VALUE_ANGLE;
        float curProgress = 0;

        float radius = mValueTextContainer.width() / 2;

        mTextMarkPointArray = new float[(int) (mTextTickCount * 4)];
        mMarkPointArray = new float[(int) (mTickCount * 4)];
        mMarkLabelArray.clear();
        int i = -1;
        int k = -1;
        while (currentAngle <= MAX_VALUE_ANGLE) {
            if (mTextMarkPointArray.length > 0) {
                mTextMarkPointArray[++i] = (float) (mValueTextContainer.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (radius - textTicksLength));
                mTextMarkPointArray[++i] = (float) (mValueTextContainer.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (radius - textTicksLength));
                mTextMarkPointArray[++i] = (float) (mValueTextContainer.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (radius));
                mTextMarkPointArray[++i] = (float) (mValueTextContainer.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (radius));

                float txtX = (float) (mValueTextContainer.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)
                        * (radius - textTicksLength - mGaugeContainer.width() / TEXT_TICK_FACTOR));
                float txtY = (float) (mValueTextContainer.centerY() - Math.sin(currentAngle / 180 * Math.PI)
                        * (radius - textTicksLength - mGaugeContainer.width() / TEXT_TICK_FACTOR));
                mMarkLabelArray.add(new Label(curProgress, txtX, txtY));
            }

            float angle = currentAngle + minorStep;
            if (angle >= MAX_VALUE_ANGLE) {
                break;
            }
            if (mMarkPointArray.length > 0) {
                mMarkPointArray[++k] = (float) (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius - ticksLength));
                mMarkPointArray[++k] = (float) (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI) * (radius - ticksLength));
                mMarkPointArray[++k] = (float) (mValueTextContainer.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius));
                mMarkPointArray[++k] = (float) (mValueTextContainer.centerY() - Math.sin(angle / 180 * Math.PI) * (radius));
            }

            currentAngle += mTextMarkStepAngle;
            curProgress += mTextTickStep;
        }

    }

    private RectF scale(RectF rect, float factor) {
        RectF result = new RectF(rect);
        float diffHorizontal = (result.right - result.left) * (factor - 1f);
        float diffVertical = (result.bottom - result.top) * (factor - 1f);

        result.top -= diffVertical / 2f;
        result.bottom += diffVertical / 2f;

        result.left -= diffHorizontal / 2f;
        result.right += diffHorizontal / 2f;
        return result;
    }

    protected class Label {
        public Label(float value, float x, float y) {
            if (mScaleFactor > 1 || mMaxValue >= 10) {
                this.mText = String.valueOf(Math.round(value));
            } else {
                this.mText = String.format("%.1f", value);
            }
            this.mX = x;
            this.mY = y;
        }

        private String mText;
        private float mX;
        private float mY;

        public String getText() {
            return mText;
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }
    }

    public double getCurrentValue() {
        return mCurrentValue;
    }

    public void setCurrentValue(double value) {
        value = value / mScaleFactor;
        if ((value < MIN_VALUE) || (value > mMaxValue)) {
            throw new IllegalArgumentException("Value is out of bounds");
        }
        if (mValueAnimator != null) {
            if (mValueAnimator.isStarted())
                mValueAnimator.cancel();
            mValueAnimator.setObjectValues(mCurrentValue, value);
            mValueAnimator.start();
        }
    }

    private void setCurrentValueUi(double valueUi) {
        mCurrentValue = valueUi;
        invalidate();
    }
}
