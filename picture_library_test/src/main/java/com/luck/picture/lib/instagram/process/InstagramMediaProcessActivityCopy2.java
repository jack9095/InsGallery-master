package com.luck.picture.lib.instagram.process;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 */
public class InstagramMediaProcessActivityCopy2 extends AppCompatActivity {

    // new  start
    protected View container;
    protected PictureSelectionConfig mPictureSelectionConfig;
    protected int colorPrimary;
    // new  end

    public static final String EXTRA_ASPECT_RATIO = "extra_aspect_ratio";
    public static final String EXTRA_SINGLE_IMAGE_FILTER = "extra_single_image_filter";
    public static final String EXTRA_SINGLE_IMAGE_SELECTION_FILTER = "extra_single_image_selection_filter";

    public static final int REQUEST_SINGLE_IMAGE_PROCESS = 339;
    public static final int REQUEST_MULTI_IMAGE_PROCESS = 440;
    public static final int REQUEST_SINGLE_VIDEO_PROCESS = 441;

    public static final int RESULT_MEDIA_PROCESS_CANCELED = 501;

    private List<LocalMedia> mSelectMedia;
    private InstagramTitleBar mTitleBar;
    private boolean isAspectRatio;

    public enum MediaType {SINGLE_VIDEO}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (savedInstanceState != null) {
//            mSelectMedia = PictureSelector.obtainSelectorList(savedInstanceState);
//        }
        if (mSelectMedia == null && getIntent() != null) {
            mSelectMedia = getIntent().getParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST);
        }

        if (mSelectMedia == null || mSelectMedia.isEmpty()) {
            finish();
        }
//        if (savedInstanceState != null) {
//            mPictureSelectionConfig = savedInstanceState.getParcelable(PictureConfig.EXTRA_CONFIG);
//        }
        if (mPictureSelectionConfig == null) {
            mPictureSelectionConfig = getIntent() != null ? getIntent().getParcelableExtra(PictureConfig.EXTRA_CONFIG) : mPictureSelectionConfig;
        }
        if (mPictureSelectionConfig == null) {
            mPictureSelectionConfig = PictureSelectionConfig.getInstance();
        }
        if (!mPictureSelectionConfig.camera) {
            setTheme(mPictureSelectionConfig.themeStyleId == 0 ? R.style.picture_default_style : mPictureSelectionConfig.themeStyleId);
        }

        if (getIntent() != null) {
            isAspectRatio = getIntent().getBooleanExtra(EXTRA_ASPECT_RATIO, false);
        }

        initView();
//        container.setBackgroundColor(colorPrimary);
//        mTitleBar.setBackgroundColor(colorPrimary);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
//            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onStart(InstagramMediaProcessActivityCopy2.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
//            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onResume(InstagramMediaProcessActivityCopy2.this);
        }
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
//            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onPause(InstagramMediaProcessActivityCopy2.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof LifecycleCallBack) {
//            ((LifecycleCallBack)((ViewGroup)container).getChildAt(0)).onDestroy(InstagramMediaProcessActivityCopy2.this);
        }
    }

    protected void initView() {
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
        setContentView(contentView);

        if (getIntent() != null) {
            isAspectRatio = getIntent().getBooleanExtra(EXTRA_ASPECT_RATIO, false);
        }
        // 视频控件
        InstagramMediaSingleVideoContainer singleVideoContainer =
                new InstagramMediaSingleVideoContainer(this, mPictureSelectionConfig, mSelectMedia.get(0), isAspectRatio);
        contentView.addView(singleVideoContainer,
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

//        mTitleBar = new InstagramTitleBar(this, mPictureSelectionConfig, MediaType.SINGLE_VIDEO);
        contentView.addView(mTitleBar);

        mTitleBar.setClickListener(new InstagramTitleBar.OnTitleBarItemOnClickListener() {
            @Override
            public void onLeftViewClick() {
                if (contentView.getChildAt(0) instanceof ProcessStateCallBack) {
//                    ((ProcessStateCallBack)contentView.getChildAt(0)).onBack(InstagramMediaProcessActivityCopy2.this);
                }
            }

            @Override
            public void onCenterViewClick(ImageView view) {
                if (contentView.getChildAt(0) instanceof ProcessStateCallBack) {
//                    ((ProcessStateCallBack)contentView.getChildAt(0)).onCenterFeature(InstagramMediaProcessActivityCopy2.this, view);
                }
            }

            @Override
            public void onRightViewClick() {
                if (contentView.getChildAt(0) instanceof ProcessStateCallBack) {
//                    ((ProcessStateCallBack)contentView.getChildAt(0)).onProcess(InstagramMediaProcessActivityCopy2.this);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectMedia.size() > 0) {
            PictureSelector.saveSelectorList(outState, mSelectMedia);
        }
    }

    @Override
    public void onBackPressed() {
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof ProcessStateCallBack) {
//            ((ProcessStateCallBack)((ViewGroup)container).getChildAt(0)).onBack(InstagramMediaProcessActivityCopy2.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (container != null && ((ViewGroup)container).getChildAt(0) instanceof ProcessStateCallBack) {
//            ((ProcessStateCallBack)((ViewGroup)container).getChildAt(0)).onActivityResult(InstagramMediaProcessActivityCopy2.this, requestCode, resultCode, data);
        }
    }

    public static void launchActivity(Activity activity, PictureSelectionConfig config, List<LocalMedia> images, Bundle extras, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), InstagramMediaProcessActivityCopy2.class);
        intent.putExtra(PictureConfig.EXTRA_CONFIG, config);
        intent.putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST,
                (ArrayList<? extends Parcelable>) images);
        if (extras != null) {
            intent.putExtras(extras);
        }
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(0, 0);
    }

    public void showLoadingView() {
    }
}
