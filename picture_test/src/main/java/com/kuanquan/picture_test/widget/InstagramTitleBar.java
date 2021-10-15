package com.kuanquan.picture_test.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.kuanquan.picture_test.R;
import com.kuanquan.picture_test.util.ScreenUtils;

/**
 * 标题栏
 */
public class InstagramTitleBar extends FrameLayout {
    private ImageView mLeftView;
    private TextView mCenterView;
    private TextView mRightView;
    private OnTitleBarItemOnClickListener mClickListener;

    public InstagramTitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InstagramTitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InstagramTitleBar(@NonNull Context context) {
        super(context);
        setBackgroundColor(ContextCompat.getColor(context, R.color.picture_color_black));
        mLeftView = new ImageView(context);
        mLeftView.setImageResource(R.drawable.discover_return);
        mLeftView.setPadding(ScreenUtils.dip2px(context, 15), 0, ScreenUtils.dip2px(context, 15), 0);
        mLeftView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onLeftViewClick();
            }
        });
        mLeftView.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.picture_color_white), PorterDuff.Mode.MULTIPLY));
        addView(mLeftView);

        mCenterView = new TextView(context);
        mCenterView.setPadding(ScreenUtils.dip2px(context, 10), 0, ScreenUtils.dip2px(context, 10), 0);
        mCenterView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_white));
        mCenterView.setTextSize(18);
        mCenterView.setGravity(Gravity.CENTER);
        mCenterView.setText(context.getString(R.string.cover));
        addView(mCenterView);

        mRightView = new TextView(context);
//        RelativeLayout.LayoutParams  lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        FrameLayout.LayoutParams  lp = (FrameLayout.LayoutParams) mRightView.getLayoutParams();
//        lp.setMargins(0, 0, ScreenUtils.dip2px(context, 16), 0);
//        mRightView.setLayoutParams(lp);
//        mRightView.setRight(ScreenUtils.dip2px(context, 16));

        mRightView.setHeight(ScreenUtils.dip2px(context, 30));
        mRightView.setWidth(ScreenUtils.dip2px(context, 70));
        mRightView.setPadding(ScreenUtils.dip2px(context, 14), ScreenUtils.dip2px(context, 5), ScreenUtils.dip2px(context, 14), ScreenUtils.dip2px(context, 5));
        mRightView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_white));
        mRightView.setTextSize(14);
        mRightView.setText(context.getString(R.string.next));
        mRightView.setGravity(Gravity.CENTER);
        mRightView.setBackground(new CommonShapeBuilder()
                .setColor(ContextCompat.getColor(context, R.color.color_ff4338))
                .setCornerRadius(ScreenUtils.dip2px(context, 15))
                .build()
        );
        mRightView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onRightViewClick();
            }
        });
        addView(mRightView);
    }

    public void setRightViewText(String text) {
        mRightView.setText(text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = ScreenUtils.dip2px(getContext(), 48);
        mLeftView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        if (mCenterView.getVisibility() == View.VISIBLE) {
            mCenterView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        int rightHeight = ScreenUtils.dip2px(getContext(), 30);
        mRightView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(rightHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mLeftView.getMeasuredHeight()) / 2;
        int viewLeft = 0;
        mLeftView.layout(viewLeft, viewTop, viewLeft + mLeftView.getMeasuredWidth(), viewTop + mLeftView.getMeasuredHeight());

        if (mCenterView.getVisibility() == View.VISIBLE) {
            viewTop = (getMeasuredHeight() - mCenterView.getMeasuredHeight()) / 2;
            viewLeft = (getMeasuredWidth() - mCenterView.getMeasuredWidth()) / 2;
            mCenterView.layout(viewLeft, viewTop, viewLeft + mCenterView.getMeasuredWidth(), viewTop + mCenterView.getMeasuredHeight());
        }

        viewTop = (getMeasuredHeight() - mRightView.getMeasuredHeight()) / 2;
        int rightMargin = ScreenUtils.dip2px(getContext(), 16);
        viewLeft = getMeasuredWidth() - mRightView.getMeasuredWidth() - rightMargin;
        mRightView.layout(viewLeft, viewTop, viewLeft + mRightView.getMeasuredWidth(), viewTop + mRightView.getMeasuredHeight());

    }

    public void setClickListener(OnTitleBarItemOnClickListener clickListener) {
        mClickListener = clickListener;
    }

    public interface OnTitleBarItemOnClickListener {
        void onLeftViewClick();
        void onRightViewClick();
    }
}
