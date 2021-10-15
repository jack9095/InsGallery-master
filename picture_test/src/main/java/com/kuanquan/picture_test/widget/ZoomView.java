package com.kuanquan.picture_test.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.appcompat.widget.AppCompatImageView;

import com.kuanquan.picture_test.util.ScreenUtils;

/**
 * 手指触摸放大的 View
 */
public class ZoomView extends AppCompatImageView {
    private Paint mPaint;
    private Bitmap mBitmap;

    public ZoomView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0XFFDDDDDD);
        mPaint.setStrokeWidth(ScreenUtils.dip2px(getContext(),2));
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }

        float right = getMeasuredWidth()- ScreenUtils.dip2px(getContext(), 1);
//        float right = getMeasuredWidth() >> 1;
        canvas.drawRect(
                ScreenUtils.dip2px(getContext(), 1),
                ScreenUtils.dip2px(getContext(), 1),
                right,
                getMeasuredHeight() - ScreenUtils.dip2px(getContext(), 1),
                mPaint);
    }
}
