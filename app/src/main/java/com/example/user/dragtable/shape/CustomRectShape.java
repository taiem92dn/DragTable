package com.example.user.dragtable.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

public class CustomRectShape extends RectShape implements HasStroke, HasHandle {

    private Paint mStrokePaint;
    private Paint mHandlePaint;
    private boolean isShowStroke;
    private boolean isShowHandle;
    private float mHandleSize;

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        if (isShowStroke) {
            super.draw(canvas, mStrokePaint);
        }

        if (isShowHandle) {
            RectF rect = rect();
            canvas.drawCircle(rect.top, rect.left, mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.bottom, rect.left, mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.top, rect.right, mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.bottom, rect.bottom, mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.top, rect.left, mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.top, rect.centerX(), mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.bottom, rect.centerX(), mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.centerY(), rect.left, mHandleSize, mHandlePaint);
            canvas.drawCircle(rect.centerY(), rect.right, mHandleSize, mHandlePaint);
        }
    }

    @Override
    public void setShowStroke(boolean pIsShowStroke) {
        isShowStroke = pIsShowStroke;
    }

    @Override
    public void setStrokePaint(Paint pStrokePaint) {
        mStrokePaint = pStrokePaint;
    }

    @Override
    public void setHandlePaint(Paint pHandlePaint) {
        mHandlePaint = pHandlePaint;
    }

    @Override
    public void setHandleSize(float pHandleSize) {
        mHandleSize = pHandleSize;
    }

    @Override
    public void setShowHandle(boolean pShowHandle) {
        isShowHandle = pShowHandle;
    }
}
