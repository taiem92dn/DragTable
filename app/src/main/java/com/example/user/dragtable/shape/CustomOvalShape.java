package com.example.user.dragtable.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;

public class CustomOvalShape extends OvalShape implements HasStroke, HasHandle {
    private Paint mStrokePaint;
    private Paint mHandlePaint;
    private boolean isShowStroke;
    private boolean isShowHandle;
    private float mHandleSize;

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        if (isShowStroke) {
            canvas.drawRect(rect(), mStrokePaint);
        }

        if (isShowHandle) {
            RectF rect = rect();
            RectF oval = new RectF();
            oval.set(rect.left-mHandleSize, rect.top-mHandleSize, rect.left+mHandleSize, rect.top+mHandleSize);
            canvas.drawArc(oval, 0, 90, true, mHandlePaint);
            oval.set(rect.centerX()-mHandleSize, rect.top-mHandleSize, rect.centerX()+mHandleSize, rect.top+mHandleSize);
            canvas.drawArc(oval, 0, 180, true, mHandlePaint);
            oval.set(rect.right-mHandleSize, rect.top-mHandleSize, rect.right+mHandleSize, rect.top+mHandleSize);
            canvas.drawArc(oval, 90, 90, true, mHandlePaint);
            oval.set(rect.right-mHandleSize, rect.centerY()-mHandleSize, rect.right+mHandleSize, rect.centerY()+mHandleSize);
            canvas.drawArc(oval, 90, 180, true, mHandlePaint);
            oval.set(rect.right-mHandleSize, rect.bottom-mHandleSize, rect.right+mHandleSize, rect.bottom+mHandleSize);
            canvas.drawArc(oval, 180, 90, true, mHandlePaint);
            oval.set(rect.centerX()-mHandleSize, rect.bottom-mHandleSize, rect.centerX()+mHandleSize, rect.bottom+mHandleSize);
            canvas.drawArc(oval, 180, 180, true, mHandlePaint);
            oval.set(rect.left-mHandleSize, rect.bottom-mHandleSize, rect.left+mHandleSize, rect.bottom+mHandleSize);
            canvas.drawArc(oval, 270, 90, true, mHandlePaint);
            oval.set(rect.left-mHandleSize, rect.centerY()-mHandleSize, rect.left+mHandleSize, rect.centerY()+mHandleSize);
            canvas.drawArc(oval, 270, 180, true, mHandlePaint);
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
