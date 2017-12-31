package com.achilles.jumpx;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * TODO: document your custom view class.
 */
public class MyLinearLayout extends LinearLayout {

    private Point point1, point2;
    private boolean mIsNeed2DrawLittleBoyRect;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制小人方框
        if (mIsNeed2DrawLittleBoyRect && point1 != null && point2 != null) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            paint.setAntiAlias(true);
            RectF rectF = new RectF(point1.x, point1.y, point2.x, point2.y);
            canvas.drawRect(rectF, paint);
        }

        //清除上一次的绘制
        if (!mIsNeed2DrawLittleBoyRect  && point1 != null && point2 != null ) {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#00000000"));
            paint.setStyle(Paint.Style.FILL);
            RectF rectF = new RectF(point1.x, point1.y, point2.x, point2.y);
            canvas.drawRect(rectF, paint);
        }
    }

    public void setPointsAndShowLittleBoyRect(Point point1, Point point2) {
        this.point1 = point1;
        this.point2 = point2;
        mIsNeed2DrawLittleBoyRect = true;
        this.invalidate();
    }

    public void clearDraw() {
        mIsNeed2DrawLittleBoyRect = false;
        this.invalidate();
    }
}
