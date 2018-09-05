package com.example.user.dragtable.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.Nullable;

public class CustomRoundRectShape extends RoundRectShape implements HasStroke {

    private Paint mStrokePaint;
    private boolean isShowStroke;

    public CustomRoundRectShape(@Nullable float[] outerRadii, @Nullable RectF inset, @Nullable float[] innerRadii) {
        super(outerRadii, inset, innerRadii);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        if (isShowStroke) {
            super.draw(canvas, mStrokePaint);
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
}
