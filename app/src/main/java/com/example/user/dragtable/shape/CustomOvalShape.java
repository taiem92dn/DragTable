package com.example.user.dragtable.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.OvalShape;

public class CustomOvalShape extends OvalShape implements HasStroke {
    private Paint mStrokePaint;
    private boolean isShowStroke;

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        if (isShowStroke) {
            super.draw(canvas, mStrokePaint);
        }
    }

    @Override
    public void isShowStroke(boolean pIsShowStroke) {
        isShowStroke = pIsShowStroke;
    }

    @Override
    public void setStrokePaint(Paint pStrokePaint) {
        mStrokePaint = pStrokePaint;
    }
}
