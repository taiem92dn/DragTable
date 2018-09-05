package com.example.user.dragtable.shape;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * Simple shape example that generates a shadow casting outline.
 */
public class TriangleShape extends Shape implements HasStroke {
    private final Path mPath = new Path();
    private Paint strokePaint;
    private boolean isShowStroke;

    @Override
    protected void onResize(float width, float height) {
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(width, 0);
        mPath.lineTo(width / 2, height);
        mPath.lineTo(0, 0);
        mPath.close();
    }
    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(mPath, paint);

        if (isShowStroke) {
            canvas.drawPath(mPath, strokePaint);
        }
    }
    @Override
    public void getOutline(@NonNull Outline outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outline.setConvexPath(mPath);
        }
    }

    @Override
    public void setShowStroke(boolean pIsShowStroke) {
        isShowStroke = pIsShowStroke;
    }

    @Override
    public void setStrokePaint(Paint pStrokePaint) {
        strokePaint = pStrokePaint;
    }
}