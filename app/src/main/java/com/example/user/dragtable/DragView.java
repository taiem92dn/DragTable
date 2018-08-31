package com.example.user.dragtable;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.example.user.dragtable.shape.CustomOvalShape;
import com.example.user.dragtable.shape.CustomRectShape;
import com.example.user.dragtable.shape.CustomRoundRectShape;
import com.example.user.dragtable.shape.HasStroke;
import com.example.user.dragtable.shape.TriangleShape;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class DragView extends TextView {

    private static final float MAX_Z_DP = 10;
    private static final float MOMENTUM_SCALE = 10;
    private static final int MAX_ANGLE = 10;

    private class CardDragState {
        long lastEventTime;
        float lastX;
        float lastY;
        float momentumX;
        float momentumY;
        View v;

        public CardDragState(View v) {
            this.v = v;
        }

        public void onDown(long eventTime, float x, float y) {
            lastEventTime = eventTime;
            lastX = x;
            lastY = y;
            momentumX = 0;
            momentumY = 0;
        }
        public void onMove(long eventTime, float x, float y) {
            final long deltaT = eventTime - lastEventTime;
            if (deltaT != 0) {
                float newMomentumX = (x - lastX) / (mDensity * deltaT);
                float newMomentumY = (y - lastY) / (mDensity * deltaT);
                momentumX = 0.9f * momentumX + 0.1f * (newMomentumX * MOMENTUM_SCALE);
                momentumY = 0.9f * momentumY + 0.1f * (newMomentumY * MOMENTUM_SCALE);
                momentumX = Math.max(Math.min((momentumX), MAX_ANGLE), -MAX_ANGLE);
                momentumY = Math.max(Math.min((momentumY), MAX_ANGLE), -MAX_ANGLE);
                //noinspection SuspiciousNameCombination
                v.setRotationX(-momentumY);
                //noinspection SuspiciousNameCombination
                v.setRotationY(momentumX);
                if (mShadingEnabled) {
                    float alphaDarkening = (momentumX * momentumX + momentumY * momentumY) / (90 * 90);
                    alphaDarkening /= 2;
                    int alphaByte = 0xff - ((int)(alphaDarkening * 255) & 0xff);
                    int color = Color.rgb(alphaByte, alphaByte, alphaByte);
                    mCardBackground.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                }
            }
            lastX = x;
            lastY = y;
            lastEventTime = eventTime;
        }
        public void onUp() {
            ObjectAnimator flattenX = ObjectAnimator.ofFloat(v, "rotationX", 0);
            flattenX.setDuration(100);
            flattenX.setInterpolator(new AccelerateInterpolator());
            flattenX.start();
            ObjectAnimator flattenY = ObjectAnimator.ofFloat(v, "rotationY", 0);
            flattenY.setDuration(100);
            flattenY.setInterpolator(new AccelerateInterpolator());
            flattenY.start();
            // set default color
            mCardBackground.setColorFilter(null);
        }
    }


    private float mDensity;
    private boolean mTiltEnabled = true;
    private boolean mShadingEnabled = false; // true will change color when dragging
    private ShapeDrawable mCardBackground = new ShapeDrawable();
    private CardDragState mDragState;
    private int mId;

    private final ArrayList<Shape> mShapes = new ArrayList<Shape>();

    public DragView(Context context) {
        super(context);
        init();
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initShapes();
        mCardBackground.getPaint().setColor(Color.WHITE);
        mCardBackground.setShape(mShapes.get(0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mCardBackground);
        }

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
//        setBackgroundResource(R.drawable.round_rect);
        setText(R.string.draggable_card);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(getDP(2));
        }


        mDragState = new CardDragState(this);
        setLayoutParams(new ViewGroup.LayoutParams(getDP(150), getDP(150)));

        /*
          Enable any touch on the parent to drag the card. Note that this doesn't do a proper hit
          test, so any drag (including off of the card) will work.

          This enables the user to see the effect more clearly for the purpose of this demo.
         */
        this.setOnTouchListener(new OnTouchListener() {
            float downX;
            float downY;
            long downTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX() - v.getTranslationX();
                        downY = event.getRawY() - v.getTranslationY();
                        downTime = event.getDownTime();
                        ObjectAnimator upAnim = ObjectAnimator.ofFloat(v, "translationZ",
                                MAX_Z_DP * mDensity);
                        upAnim.setDuration(100);
                        upAnim.setInterpolator(new DecelerateInterpolator());
                        upAnim.start();
                        if (mTiltEnabled) {
                            mDragState.onDown(event.getDownTime(), event.getRawX(), event.getRawY());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        v.setTranslationX(event.getRawX() - downX);
                        v.setTranslationY(event.getRawY() - downY);
                        if (mTiltEnabled) {
                            mDragState.onMove(event.getEventTime(), event.getRawX(), event.getRawY());
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        ObjectAnimator downAnim = ObjectAnimator.ofFloat(v, "translationZ", 0);
                        downAnim.setDuration(100);
                        downAnim.setInterpolator(new AccelerateInterpolator());
                        downAnim.start();
                        if (mTiltEnabled) {
                            mDragState.onUp();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void initShapes() {
        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.parseColor("#c2c2c2"));
        strokePaint.setStrokeWidth(10);
        strokePaint.setStyle(Paint.Style.STROKE);

        TriangleShape triangleShape = new TriangleShape();

        RectShape rectShape = new CustomRectShape();

        OvalShape ovalShape = new CustomOvalShape();

        mDensity = getResources().getDisplayMetrics().density;
        float r = 10 * mDensity;
        float radii[] = new float[] {r, r, r, r, r, r, r, r};
        RoundRectShape roundRectShape = new CustomRoundRectShape(radii, null, null);

        mShapes.add(rectShape);
        mShapes.add(ovalShape);
        mShapes.add(roundRectShape);
        mShapes.add(triangleShape);

        for (Shape shape : mShapes) {
            if (shape instanceof HasStroke) {
                ((HasStroke) shape).setStrokePaint(strokePaint);
            }
        }
    }

    int getDP(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean isTiltEnabled() {
        return mTiltEnabled;
    }

    public void setTiltEnabled(boolean pTiltEnabled) {
        mTiltEnabled = pTiltEnabled;
        if (!mTiltEnabled) {
            mDragState.onUp();
        }
    }

    public boolean isShadingEnabled() {
        return mShadingEnabled;
    }

    public void setShadingEnabled(boolean pShadingEnabled) {
        mShadingEnabled = pShadingEnabled;
        if (!mShadingEnabled) {
            mCardBackground.setColorFilter(null);
        }
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public void setId(int pId) {
        mId = pId;
        setText("T" + (pId+1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(getElevation() + pId/3.0f);
        }
    }


    int index = 0;
    public void changeShape() {
        index = (index + 1) % mShapes.size();
        if (mCardBackground.getShape() instanceof HasStroke) {
            ((HasStroke) mShapes.get(index)).isShowStroke(isSelected());
        }
        mCardBackground.setShape(mShapes.get(index));
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (mCardBackground.getShape() instanceof HasStroke) {
            ((HasStroke) mCardBackground.getShape()).isShowStroke(selected);
            mCardBackground.invalidateSelf();
        }
    }

    public void setShapeColor(int pColor) {
        mCardBackground.getPaint().setColor(pColor);
        mCardBackground.invalidateSelf();
    }
}
