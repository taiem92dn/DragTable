package com.example.user.dragtable;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.user.dragtable.shape.CustomOvalShape;
import com.example.user.dragtable.shape.CustomRectShape;
import com.example.user.dragtable.shape.CustomRoundRectShape;
import com.example.user.dragtable.shape.HasHandle;
import com.example.user.dragtable.shape.HasStroke;
import com.example.user.dragtable.shape.TriangleShape;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class DragView extends TextView implements View.OnTouchListener {

    private static final String TAG = DragView.class.getSimpleName();

    private static final float MAX_Z_DP = 10;
    private static final float MOMENTUM_SCALE = 10;
    private static final int MAX_ANGLE = 10;

    private static final int HANDLE_SIZE_IN_DP = 25;
    private static final int MIN_FRAME_SIZE_IN_DP = 70;
    private TouchArea mTouchArea;

    private boolean isEditMode;
    private float mLastX;
    private float mLastY;
    private float mMinSize;

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
    private float mHandleSize;
    private int mTouchPadding = 0;
    private Rect mFrameRect = new Rect();
    private Rect mParentFrameRect = new Rect();

    float downX;
    float downY;
    long downTime;

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
        mMinSize = mDensity * MIN_FRAME_SIZE_IN_DP;

        mCardBackground.getPaint().setColor(Color.WHITE);
        mCardBackground.setShape(mShapes.get(0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mCardBackground);
        }

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
//        setBackgroundResource(R.drawable.round_rect);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(getDP(2));
        }


        mDragState = new CardDragState(this);
        setSize(getDP(100), getDP(100));

        this.setOnTouchListener(this);
    }

    private void setSize(int width, int height) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(width, height);
        }
        else {
            // check scale bound
            int newR = mFrameRect.left + width;
            int newB = mFrameRect.top + height;

            if (newR > mParentFrameRect.right) {
                width = mParentFrameRect.right - mFrameRect.left;
            }

            if (newB > mParentFrameRect.bottom) {
                height = mParentFrameRect.bottom - mFrameRect.top;
            }

            layoutParams.width = width;
            layoutParams.height = height;
        }
        setLayoutParams(layoutParams);

    }

    @Override
    public void setTranslationX(float translationX) {
        // check scale bound
        float newL = translationX + mParentFrameRect.left;
        float newR = translationX + getWidth() + mParentFrameRect.left;

        if (newL < mParentFrameRect.left) {
            translationX = 0;
        }

        if (newR > mParentFrameRect.right) {
            translationX = mParentFrameRect.right - mParentFrameRect.left - getWidth();
        }

        super.setTranslationX(translationX);
    }

    @Override
    public void setTranslationY(float translationY) {
        // check scale bound
        float newT = translationY + mParentFrameRect.top;
        float newB = translationY + getHeight() + mParentFrameRect.top;

        if (newT < mParentFrameRect.top) {
            translationY = 0;
        }

        if (newB > mParentFrameRect.bottom) {
            translationY = mParentFrameRect.bottom - mParentFrameRect.top - getHeight();
        }


        super.setTranslationY(translationY);
    }

    private void initShapes() {
        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.parseColor("#dedede"));
        strokePaint.setStrokeWidth(5);
        strokePaint.setStyle(Paint.Style.STROKE);

        Paint handlePaint = new Paint();
        handlePaint.setColor(Color.parseColor("#f0f0f0"));
        handlePaint.setStyle(Paint.Style.FILL);


        RectShape rectShape = new CustomRectShape();

        OvalShape ovalShape = new CustomOvalShape();

        mDensity = getResources().getDisplayMetrics().density;
        float r = 10 * mDensity;
        float radii[] = new float[] {r, r, r, r, r, r, r, r};
        RoundRectShape roundRectShape = new CustomRoundRectShape(radii, null, null);

        mHandleSize = HANDLE_SIZE_IN_DP * mDensity;

        mShapes.add(rectShape);
        mShapes.add(ovalShape);
//        mShapes.add(roundRectShape);
//        mShapes.add(triangleShape);

        for (Shape shape : mShapes) {
            if (shape instanceof HasStroke) {
                ((HasStroke) shape).setStrokePaint(strokePaint);
            }
            if (shape instanceof HasHandle) {
                ((HasHandle) shape).setHandleSize(mHandleSize);
                ((HasHandle) shape).setHandlePaint(handlePaint);
            }
        }
    }

    /**
     Enable any touch on the parent to drag the card. Note that this doesn't do a proper hit
     test, so any drag (including off of the card) will work.

     This enables the user to see the effect more clearly for the purpose of this demo.
     **/
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isEditMode) return false;

        getGlobalVisibleRect(mFrameRect);
        ((View) getParent()).getGlobalVisibleRect(mParentFrameRect);

        Log.d(TAG, "onTouch: " + event.getRawX() + ", " + event.getRawY());
        Log.d(TAG, "parent frame: " + mParentFrameRect);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkTouchArea(event.getRawX(), event.getRawY());
                if (isResizing()) {
                    onDownResizing(event);
                }
                else {
                    onDown(v, event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isResizing()) {
                    onMoveResizing(event);
                }
                else {
                    onMove(v, event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isResizing()) {
                    onUpResizing(event);
                }
                else {
                    onUp(v, event);
                }
                break;
        }
        return false;
    }

    private void onDown(View v, MotionEvent event) {
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
    }

    private void onMove(View v, MotionEvent event) {
        v.setTranslationX(event.getRawX() - downX);
        v.setTranslationY(event.getRawY() - downY);
        if (mTiltEnabled) {
            mDragState.onMove(event.getEventTime(), event.getRawX(), event.getRawY());
        }
    }

    private void onUp(View v, MotionEvent event) {
        ObjectAnimator downAnim = ObjectAnimator.ofFloat(v, "translationZ", 0);
        downAnim.setDuration(100);
        downAnim.setInterpolator(new AccelerateInterpolator());
        downAnim.start();
        if (mTiltEnabled) {
            mDragState.onUp();
        }
    }

    private void onDownResizing(MotionEvent e) {
        mLastX = e.getRawX();
        mLastY = e.getRawY();
    }

    private void onMoveResizing(MotionEvent e) {
        float diffX = e.getRawX() - mLastX;
        float diffY = e.getRawY() - mLastY;
        switch (mTouchArea) {
            case LEFT_TOP:
                moveHandleLT(diffX, diffY);
                break;
            case RIGHT_TOP:
                moveHandleRT(diffX, diffY);
                break;
            case LEFT_BOTTOM:
                moveHandleLB(diffX, diffY);
                break;
            case RIGHT_BOTTOM:
                moveHandleRB(diffX, diffY);
                break;
            case LEFT_CENTER:
                moveHandleLC(diffX, diffY);
                break;
            case RIGHT_CENTER:
                moveHandleRC(diffX, diffY);
                break;
            case CENTER_BOTTOM:
                moveHandleCB(diffX, diffY);
                break;
            case CENTER_TOP:
                moveHandleCT(diffX, diffY);
                break;
            case OUT_OF_BOUNDS:
                break;
        }
        invalidate();
        mLastX = e.getRawX();
        mLastY = e.getRawY();
    }

    private void onUpResizing(MotionEvent e) {
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
    }

    private void moveHandleLT(float diffX, float diffY) {
        int newL = checkScaleBoundLeft(mFrameRect.left + (int) diffX);
        int newT = checkScaleBoundTop(mFrameRect.top + (int) diffY);

        int newW = mFrameRect.right - newL;
        int newH = mFrameRect.bottom - newT;

        if (isWidthTooSmall(newW)) {
            float offsetX = mMinSize - newW;
            newL -= offsetX;
        }
        if (isHeightTooSmall(newH)) {
            float offsetY = mMinSize - newH;
            newT -= offsetY;
        }

        newW = mFrameRect.right - newL;
        newH = mFrameRect.bottom - newT;
        setSize(newW, newH);
        setTranslationX(getTranslationX() + (newL - mFrameRect.left));
        setTranslationY(getTranslationY() + (newT - mFrameRect.top));
    }

    private void moveHandleLC(float diffX, float diffY) {
        int newL = checkScaleBoundLeft(mFrameRect.left + (int) diffX);

        int newW = mFrameRect.right - newL;

        if (isWidthTooSmall(newW)) {
            float offsetX = mMinSize - newW;
            newL -= offsetX;
        }

        newW = mFrameRect.right - newL;
        setSize(newW, getHeight());
        setTranslationX(getTranslationX() + (newL - mFrameRect.left));
    }

    private void moveHandleRT(float diffX, float diffY) {
        int newR = checkScaleBoundRight(mFrameRect.right + (int) diffX);
        int newT = checkScaleBoundTop(mFrameRect.top + (int) diffY);

        int newW = newR - mFrameRect.left;
        int newH = mFrameRect.bottom - newT;

        if (isWidthTooSmall(newW)) {
            float offsetX = mMinSize - newW;
            newR += offsetX;
        }
        if (isHeightTooSmall(newH)) {
            float offsetY = mMinSize - newH;
            newT -= offsetY;
        }

        newW = newR - mFrameRect.left;
        newH = mFrameRect.bottom - newT;
        setSize(newW, newH);
        setTranslationY(getTranslationY() + (newT - mFrameRect.top));
    }

    private void moveHandleRC(float diffX, float diffY) {
        int newR = checkScaleBoundRight(mFrameRect.right + (int) diffX);

        int newW = newR - mFrameRect.left;

        if (isWidthTooSmall(newW)) {
            float offsetX = mMinSize - newW;
            newR += offsetX;
        }

        newW = newR - mFrameRect.left;
        setSize(newW, getHeight());
    }

    private void moveHandleLB(float diffX, float diffY) {
        int newL = checkScaleBoundLeft(mFrameRect.left + (int) diffX);
        int newB = checkScaleBoundBottom(mFrameRect.bottom + (int) diffY);

        int newW = mFrameRect.right - newL;
        int newH = newB - mFrameRect.top;

        if (isWidthTooSmall(newW)) {
            float offsetX = mMinSize - newW;
            newL -= offsetX;
        }
        if (isHeightTooSmall(newH)) {
            float offsetY = mMinSize - newH;
            newB += offsetY;
        }

        newW = mFrameRect.right - newL;
        newH = newB - mFrameRect.top;
//        checkScaleBounds();
        setSize(newW, newH);
        setTranslationX(getTranslationX() + (newL - mFrameRect.left));
    }

    private void moveHandleCB(float diffX, float diffY) {
        int newB = checkScaleBoundBottom(mFrameRect.bottom + (int) diffY);

        int newH = newB - mFrameRect.top;
        if (isHeightTooSmall(newH)) {
            float offsetY = mMinSize - newH;
            newB += offsetY;
        }
        newH = newB - mFrameRect.top;

        setSize(getWidth(), newH);
    }

    private void moveHandleRB(float diffX, float diffY) {
        float newW = getWidth() + diffX;
        float newH = getHeight() + diffY;
        if (isWidthTooSmall(newW)) {
            newW = mMinSize;
        }
        if (isHeightTooSmall(newH)) {
            newH = mMinSize;
        }
//        checkScaleBounds();
        setSize((int) newW, (int) newH);
    }

    private void moveHandleCT(float diffX, float diffY) {
        int newT = checkScaleBoundTop(mFrameRect.top + (int) diffY);

        int newH = mFrameRect.bottom - newT;

        if (isHeightTooSmall(newH)) {
            float offsetY = mMinSize - newH;
            newT -= offsetY;
        }

        newH = mFrameRect.bottom - newT;

        setSize(getWidth(), newH);
        setTranslationY(getTranslationY() + (newT - mFrameRect.top));
    }

    private int checkScaleBoundLeft(int left) {
        if (left < mParentFrameRect.left) {
            return mParentFrameRect.left;
        }
        return left;
    }

    private int checkScaleBoundRight(int right) {
        if (right > mParentFrameRect.right) {
            return mParentFrameRect.right;
        }
        return right;
    }

    private int checkScaleBoundTop(int top) {
        if (top < mParentFrameRect.top) {
            return mParentFrameRect.top;
        }
        return top;
    }

    private int checkScaleBoundBottom(int bottom) {
        if (bottom > mParentFrameRect.bottom) {
            return mParentFrameRect.bottom;
        }
        return bottom;
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

    public void setEditMode(boolean pEditMode) {
        isEditMode = pEditMode;
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
            ((HasStroke) mShapes.get(index)).setShowStroke(isSelected());
        }
        mCardBackground.setShape(mShapes.get(index));
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if (!isEditMode) return;

        if (mCardBackground.getShape() instanceof HasStroke) {
            ((HasStroke) mCardBackground.getShape()).setShowStroke(selected);
            mCardBackground.invalidateSelf();
        }

        if (mCardBackground.getShape() instanceof HasHandle) {
            ((HasHandle) mCardBackground.getShape()).setShowHandle(selected);
            mCardBackground.invalidateSelf();
        }

        if (selected) {
            getParent().bringChildToFront(this);
        }

        // test commit
    }

    public void setShapeColor(int pColor) {
        mCardBackground.getPaint().setColor(pColor);
        mCardBackground.invalidateSelf();
    }

    public void setTouchPaddingInDp(int paddingDp) {
        mTouchPadding = (int) (paddingDp * mDensity);
    }

    private void checkTouchArea(float x, float y) {
        if (isInsideCornerLeftTop(x, y)) {
            mTouchArea = TouchArea.LEFT_TOP;
            return;
        }
        if (isInsideCornerRightTop(x, y)) {
            mTouchArea = TouchArea.RIGHT_TOP;
            return;
        }
        if (isInsideCornerLeftBottom(x, y)) {
            mTouchArea = TouchArea.LEFT_BOTTOM;
            return;
        }
        if (isInsideCornerRightBottom(x, y)) {
            mTouchArea = TouchArea.RIGHT_BOTTOM;
            return;
        }
        if (isInsideLeftCenter(x, y)) {
            mTouchArea = TouchArea.LEFT_CENTER;
            return;
        }
        if (isInsideRightCenter(x, y)) {
            mTouchArea = TouchArea.RIGHT_CENTER;
            return;
        }
        if (isInsideCenterTop(x, y)) {
            mTouchArea = TouchArea.CENTER_TOP;
            return;
        }
        if (isInsideCenterBottom(x, y)) {
            mTouchArea = TouchArea.CENTER_BOTTOM;
            return;
        }

        mTouchArea = TouchArea.CENTER;
    }

    private boolean isInsideCornerLeftTop(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerRightTop(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerLeftBottom(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerRightBottom(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideLeftCenter(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.centerY();
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideRightCenter(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.centerY();
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCenterTop(float x, float y) {
        float dx = x - mFrameRect.centerX();
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCenterBottom(float x, float y) {
        float dx = x - mFrameRect.centerX();
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isWidthTooSmall(float width) {
        return width < mMinSize;
    }

    private boolean isHeightTooSmall(float height) {
        return height < mMinSize;
    }

    private boolean isResizing() {
        return mTouchArea != TouchArea.CENTER;
    }

    private float sq(float value) {
        return value * value;
    }

    private enum TouchArea {
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM, LEFT_CENTER, RIGHT_CENTER, CENTER_TOP, CENTER_BOTTOM
    }
}
