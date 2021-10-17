package com.kuanquan.picture_test.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kuanquan.picture_test.R;
import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.picture_test.task.GetAllFrameTask;
import com.kuanquan.picture_test.task.GetFrameBitmapTask;
import com.kuanquan.picture_test.thread.PictureThreadUtils;
import com.kuanquan.picture_test.util.ScreenUtils;
import com.kuanquan.picture_test.util.SdkVersionUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * 视频封面选择器
 */
public class CoverContainer extends FrameLayout {
    private final ImageView[] mImageViews = new ImageView[10]; // 把视频几等份的图片集合，这里是 10等份
    private int mImageViewHeight; // 展示图片控件的高度，写死的 60dp
    private int mImageViewWidth;  // 展示图片控件的宽度
    private GetAllFrameTask mFrameTask;
    private View mMaskView;  // 未被选择的图片上面的蒙层View，百分之70 的透明度
    private ZoomView mZoomView; // 选中图片上面的蒙板View,可以跟着手指滑动
    private int startedTrackingX; // X轴跟踪手指移动的坐标
    private float scrollHorizontalPosition; // 当前实时水平滑动的位置（x轴坐标）
    private onSeekListener mOnSeekListener;
    private LocalMedia mLocalMedia; // 传进来的数据，包含视频的路径
    private long mChangeTime;
    private GetFrameBitmapTask mGetFrameBitmapTask; // 解析视频帧图片的任务
    private float mCurrentPercent;  // 当前的百分比
    public MutableLiveData<String> mLiveData = new MutableLiveData<>();

    public CoverContainer(@NonNull Context context, LocalMedia media) {
        super(context);
        mLocalMedia = media;
        mImageViewHeight = ScreenUtils.dip2px(getContext(), 60);
        // 创建展示图片的 View ,并添加到容器中，这里会创建10个出来
        for (int i = 0; i < mImageViews.length; i++) {
            mImageViews[i] = new ImageView(context);
            mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImageViews[i].setImageResource((R.drawable.picture_image_placeholder));
            addView(mImageViews[i]);
        }
        // 创建未被选择的图片上面的蒙层View，百分之70 的透明度
        mMaskView = new View(context);
        mMaskView.setBackgroundColor(0x77FFFFFF);
        addView(mMaskView);
        // 选中图片上面的蒙板View,可以跟着手指滑动
        mZoomView = new ZoomView(context);
        addView(mZoomView);
    }

    public CoverContainer(@NonNull Context context) {
        super(context);
    }

    public CoverContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void getFrame(@NonNull Context context, LocalMedia media) {
        mGetFrameBitmapTask = new GetFrameBitmapTask(context, media, false, -1, mImageViewHeight, mImageViewHeight, new OnCompleteListenerImpl(mZoomView));
        mGetFrameBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mFrameTask = new GetAllFrameTask(context, media, mImageViews.length, 0, (int) media.getDuration(), new OnSingleBitmapListenerImpl(this));
        mFrameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mImageViewWidth = (width - ScreenUtils.dip2px(getContext(), 40)) / mImageViews.length;
        for (ImageView imageView : mImageViews) {
            imageView.measure(MeasureSpec.makeMeasureSpec(mImageViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mImageViewHeight, MeasureSpec.EXACTLY));
        }

        int maskViewWidth = width - ScreenUtils.dip2px(getContext(), 40) + mImageViews.length - 1;
        mMaskView.measure(
                MeasureSpec.makeMeasureSpec(maskViewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mImageViewHeight, MeasureSpec.EXACTLY));
//        mZoomView.measure(MeasureSpec.makeMeasureSpec(mImageViewHeight, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mImageViewHeight, MeasureSpec.EXACTLY));
        mZoomView.measure(MeasureSpec.makeMeasureSpec(mImageViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mImageViewHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mImageViewHeight) / 2;
        int viewLeft;

        // 布局，使控件距离左右各20dp
        for (int i = 0; i < mImageViews.length; i++) {
            viewLeft = i * (mImageViewWidth + 1) + ScreenUtils.dip2px(getContext(), 20);
            mImageViews[i].layout(viewLeft, viewTop, viewLeft + mImageViews[i].getMeasuredWidth(), viewTop + mImageViews[i].getMeasuredHeight());
        }

        viewLeft = ScreenUtils.dip2px(getContext(), 20);
        mMaskView.layout(viewLeft, viewTop, viewLeft + mMaskView.getMeasuredWidth(), viewTop + mMaskView.getMeasuredHeight());

        mZoomView.layout(viewLeft, viewTop, viewLeft + mZoomView.getMeasuredWidth(), viewTop + mZoomView.getMeasuredHeight());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Rect rect = new Rect();
        mMaskView.getHitRect(rect);

        // 限制手指滑动范围的，滑动不再封面图控件上就不响应事件
//        if (!rect.contains((int) (event.getX()), (int) (event.getY()))) {
//            return super.onTouchEvent(event);
//        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startedTrackingX = (int) event.getX();
            moveByX(startedTrackingX - ScreenUtils.dip2px(getContext(), 20) - mZoomView.getMeasuredWidth() / 2);

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = (int) (event.getX() - startedTrackingX);
            moveByX(dx);
            startedTrackingX = (int) event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (mOnSeekListener != null) {
                mOnSeekListener.onSeekEnd();
            }
        }
        return true;
    }

    public void moveByX(float dx) {
        setScrollHorizontalPosition(scrollHorizontalPosition + dx);
    }

    /**
     * 设置 X 轴上滑动的坐标
     * @param value x轴坐标值
     */
    public void setScrollHorizontalPosition(float value) {
        float oldHorizontalPosition = scrollHorizontalPosition;
        scrollHorizontalPosition = Math.min(Math.max(0, value), mMaskView.getMeasuredWidth() - mZoomView.getMeasuredWidth());

        if (oldHorizontalPosition == scrollHorizontalPosition) {
            return;
        }

        mZoomView.setTranslationX(scrollHorizontalPosition);

        mCurrentPercent = scrollHorizontalPosition / (mMaskView.getMeasuredWidth() - mZoomView.getMeasuredWidth());

        if (SystemClock.uptimeMillis() - mChangeTime > 100) {
            mChangeTime = SystemClock.uptimeMillis();

            // 返回一个数字四舍五入后最接近的整数，获取到当前视屏点的毫秒值
            long time = Math.round(mLocalMedia.getDuration() * mCurrentPercent * 1000);
            mGetFrameBitmapTask = new GetFrameBitmapTask(getContext(), mLocalMedia, false, time, mImageViewHeight, mImageViewHeight, new OnCompleteListenerImpl(mZoomView));
            mGetFrameBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (mOnSeekListener != null) {
            mOnSeekListener.onSeek(mCurrentPercent);
        }
    }

    String scanFilePath; // 保存后扫描到的图片路径
    public void cropCover(CountDownLatch count) {
        long time;
        if (mCurrentPercent > 0) {
            time = Math.round(mLocalMedia.getDuration() * mCurrentPercent * 1000);
        } else {
            time = -1;
        }
        new GetFrameBitmapTask(getContext(), mLocalMedia, false, time,
                bitmap -> PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<File>() {

            @Override
            public File doInBackground() {
                String fileName = System.currentTimeMillis() + ".jpg";
                File path = getContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File file = new File(path, "Covers/" + fileName);
                OutputStream outputStream = null;
                try {
                    file.getParentFile().mkdirs();
                    outputStream = getContext().getApplicationContext().getContentResolver().openOutputStream(Uri.fromFile(file));
                    // 压缩图片 80 是压缩率，表示压缩20%; 如果不压缩是100，表示压缩率为0，把图片压缩到 outputStream 所在的文件夹下
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    bitmap.recycle();
                    MediaScannerConnection.scanFile(
                            getContext().getApplicationContext(),
                            new String[]{file.toString()}, null,
                            (path1, uri) -> {
                                scanFilePath = path1;
                                EventBus.getDefault().post(path1);
                                mLocalMedia.setCoverPath(path1);
                                count.countDown();
                            }
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    SdkVersionUtils.close(outputStream);
                }
                return null;
            }

            @Override
            public void onSuccess(File result) {
                // TODO finish 当前页面
                mLiveData.setValue(scanFilePath);
            }
        })).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onDestroy() {
        if (mFrameTask != null) {
            mFrameTask.setStop(true);
            mFrameTask.cancel(true);
            mFrameTask = null;
        }
        if (mGetFrameBitmapTask != null) {
            mGetFrameBitmapTask.cancel(true);
            mGetFrameBitmapTask = null;
        }

    }

    public static class OnSingleBitmapListenerImpl implements GetAllFrameTask.OnSingleBitmapListener {
        private WeakReference<CoverContainer> mContainerWeakReference;
        private int index;

        public OnSingleBitmapListenerImpl(CoverContainer coverContainer) {
            mContainerWeakReference = new WeakReference<>(coverContainer);
        }


        @Override
        public void onSingleBitmapComplete(Bitmap bitmap) {
            CoverContainer container = mContainerWeakReference.get();
            if (container != null) {
                container.post(new RunnableImpl(container.mImageViews[index], bitmap));
                index++;
            }
        }

        public static class RunnableImpl implements Runnable {
            private WeakReference<ImageView> mViewWeakReference;
            private Bitmap mBitmap;

            public RunnableImpl(ImageView imageView, Bitmap bitmap) {
                mViewWeakReference = new WeakReference<>(imageView);
                mBitmap = bitmap;
            }

            @Override
            public void run() {
                ImageView imageView = mViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(mBitmap);
                }
            }
        }
    }

    public static class OnCompleteListenerImpl implements GetFrameBitmapTask.OnCompleteListener {
        private WeakReference<ZoomView> mViewWeakReference;

        public OnCompleteListenerImpl(ZoomView view) {
            mViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void onGetBitmapComplete(Bitmap bitmap) {
            ZoomView view = mViewWeakReference.get();
            if (view != null) {
                view.setBitmap(bitmap);
            }
        }
    }

    public void setOnSeekListener(onSeekListener onSeekListener) {
        mOnSeekListener = onSeekListener;
    }

    public interface onSeekListener {
        void onSeek(float percent);
        void onSeekEnd();
    }
}
