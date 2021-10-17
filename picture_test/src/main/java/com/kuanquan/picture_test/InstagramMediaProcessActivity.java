package com.kuanquan.picture_test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.kuanquan.picture_test.callback.LifecycleCallBack;
import com.kuanquan.picture_test.callback.ProcessStateCallBack;
import com.kuanquan.picture_test.config.PictureConfig;
import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.picture_test.widget.InstagramMediaSingleVideoContainer;
import com.kuanquan.picture_test.util.InstagramTitleBar;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 */
public class InstagramMediaProcessActivity extends AppCompatActivity {

    protected View container;
    private List<LocalMedia> mSelectMedia;
    private InstagramTitleBar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSelectMedia == null && getIntent() != null) {
            mSelectMedia = getIntent().getParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST);
        }

        if (mSelectMedia == null || mSelectMedia.isEmpty()) {
            finish();
        }

        initView();
    }

    private void initView() {
        FrameLayout contentView = new FrameLayout(this) {

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);

                measureChild(mTitleBar, widthMeasureSpec, heightMeasureSpec);
                getChildAt(0).measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - mTitleBar.getMeasuredHeight(), MeasureSpec.EXACTLY));
                setMeasuredDimension(width, height);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                mTitleBar.layout(0, 0, mTitleBar.getMeasuredWidth(), mTitleBar.getMeasuredHeight());
                View child = getChildAt(0);
                child.layout(0, mTitleBar.getMeasuredHeight(), child.getMeasuredWidth(), mTitleBar.getMeasuredHeight() + child.getMeasuredHeight());
            }
        };
        container = contentView;
        container.setBackgroundColor(Color.parseColor("#000000"));
        setContentView(contentView);

        // 视频控件
        InstagramMediaSingleVideoContainer singleVideoContainer =
                new InstagramMediaSingleVideoContainer(this, mSelectMedia.get(0), false);
        contentView.addView(singleVideoContainer,
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        singleVideoContainer.mCoverView.mLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                finish();
            }
        });

        mTitleBar = new InstagramTitleBar(this);
        contentView.addView(mTitleBar);

        mTitleBar.setClickListener(new InstagramTitleBar.OnTitleBarItemOnClickListener() {
            @Override
            public void onLeftViewClick() {
                finish();
            }

            @Override
            public void onRightViewClick() {
                if (contentView.getChildAt(0) instanceof ProcessStateCallBack) {
                    ((ProcessStateCallBack)contentView.getChildAt(0)).onProcess(InstagramMediaProcessActivity.this);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onStart(InstagramMediaProcessActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onResume(InstagramMediaProcessActivity.this);
        }
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onPause(InstagramMediaProcessActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onDestroy(InstagramMediaProcessActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof ProcessStateCallBack) {
            ((ProcessStateCallBack)((ViewGroup)container).getChildAt(0)).onActivityResult(InstagramMediaProcessActivity.this, requestCode, resultCode, data);
        }
    }

    public static void launchActivity(Activity activity, List<LocalMedia> images) {
        Intent intent = new Intent(activity.getApplicationContext(), InstagramMediaProcessActivity.class);
        intent.putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST,
                (ArrayList<? extends Parcelable>) images);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

}
