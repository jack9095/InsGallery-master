package com.kuanquan.picture_test.copy;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.kuanquan.picture_test.R;
import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.picture_test.task.GetAllFrameTask;
import com.kuanquan.picture_test.task.GetFrameBitmapTask;
import com.kuanquan.picture_test.thread.PictureThreadUtils;
import com.kuanquan.picture_test.util.ScreenUtils;
import com.kuanquan.picture_test.util.SdkVersionUtils;
import com.kuanquan.picture_test.widget.ZoomView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * 视频封面选择器
 */
public class CoverContainer2 extends FrameLayout {
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
    int startClickX; // 点击确认选中图片上面的蒙板View的x轴位置

    ArrayList<Bitmap> bitmaps = new ArrayList<>();

    public CoverContainer2(@NonNull Context context, LocalMedia media) {
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

    public CoverContainer2(@NonNull Context context) {
        super(context);
    }

    public CoverContainer2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverContainer2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void getFrame(@NonNull Context context, LocalMedia media) {

        // 给手指触摸移动的选中view设置显示的图片 一进来mZoomView初始值
        mGetFrameBitmapTask = new GetFrameBitmapTask(context, media, false, 0, mImageViewHeight, mImageViewHeight, new OnCompleteListenerImpl(mZoomView));
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

    float dxMove;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Rect rect = new Rect();
        mMaskView.getHitRect(rect);

        // 限制手指滑动范围的，滑动不再封面图控件上就不响应事件
//        if (!rect.contains((int) (event.getX()), (int) (event.getY()))) {
//            return super.onTouchEvent(event);
//        }
        if (mOnSeekListener != null) {
            mOnSeekListener.onSeekEnd();
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startedTrackingX = (int) event.getX();
            startClickX = (int) event.getX();

            setScrollHorizontalPosition(startClickX - ScreenUtils.dip2px(getContext(), 20) - mZoomView.getMeasuredWidth() / 2);

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            dxMove = (int) (event.getX() - startedTrackingX);
            moveByX(dxMove);
            startedTrackingX = (int) event.getX();
//            postDelayed(() -> {
//                if (mOnSeekListener != null) {
//                    mOnSeekListener.onSeekEnd();
//                }
//            }, 80);
            if (mOnSeekListener != null) {
                mOnSeekListener.onSeekEnd();
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mOnSeekListener != null) {
                mOnSeekListener.onSeekEnd();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mOnSeekListener != null) {
                mOnSeekListener.onSeek(mCurrentPercent, true);
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
//                    moveByXX(dxMove);
                }
            }, 200);
        }
        return true;
    }

    public void moveByX(float dx) {
        setScrollHorizontalPosition(scrollHorizontalPosition + dx);
    }

    public void moveByXX(float dx) {
        setScrollHorizontalPositionX(scrollHorizontalPosition + dx);
    }

    /**
     * 设置 X 轴上滑动的坐标
     * @param value x轴坐标值
     */
    public void setScrollHorizontalPositionX(float value) {

        Log.e("当前滑动的x轴位置", "x -> "+ scrollHorizontalPosition);
        Log.e("当前滑动的x轴百分比", "mCurrentPercent -> "+ mCurrentPercent*100);

        if (mOnSeekListener != null) {
            mOnSeekListener.onSeek(mCurrentPercent, true);
        }

        if (mCurrentPercent*100 <= 10) {
            getZoomViewBitmap();
            return;
        } else if (mCurrentPercent*100 >= 95) {
            getZoomViewBitmap();
            return;
        }

        if (SystemClock.uptimeMillis() - mChangeTime > 200) {
            mChangeTime = SystemClock.uptimeMillis();
            getZoomViewBitmap();
        }

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

        Log.e("当前滑动的x轴位置", "x -> "+ scrollHorizontalPosition);
        Log.e("当前滑动的x轴百分比", "mCurrentPercent -> "+ mCurrentPercent*100);

//        if (mCurrentPercent*100 <= 10) {
//            mZoomView.setBitmap(bitmaps.get(0)); // 给手指触摸移动的选中view设置显示的图片
//        } else if (mCurrentPercent*100 <= 20) {
//            mZoomView.setBitmap(bitmaps.get(1));
//        } else if (mCurrentPercent*100 <= 30) {
//            mZoomView.setBitmap(bitmaps.get(2));
//        } else if (mCurrentPercent*100 <= 40) {
//            mZoomView.setBitmap(bitmaps.get(3));
//        } else if (mCurrentPercent*100 <= 50) {
//            mZoomView.setBitmap(bitmaps.get(4));
//        } else if (mCurrentPercent*100 <= 60) {
//            mZoomView.setBitmap(bitmaps.get(5));
//        } else if (mCurrentPercent*100 <= 70) {
//            mZoomView.setBitmap(bitmaps.get(6));
//        } else if (mCurrentPercent*100 <= 80) {
//            mZoomView.setBitmap(bitmaps.get(7));
//        } else if (mCurrentPercent*100 <= 90) {
//            mZoomView.setBitmap(bitmaps.get(8));
//        } else if (mCurrentPercent*100 <= 100) {
//            mZoomView.setBitmap(bitmaps.get(9));
//        }

        if (mOnSeekListener != null) {
            mOnSeekListener.onSeek(mCurrentPercent, true);
        }

        if (mCurrentPercent*100 <= 10) {
            getZoomViewBitmap();
            return;
        } else if (mCurrentPercent*100 >= 95) {
            getZoomViewBitmap();
            return;
        }

        if (SystemClock.uptimeMillis() - mChangeTime > 200) {
            mChangeTime = SystemClock.uptimeMillis();
            getZoomViewBitmap();
        }

//        if (mOnSeekListener != null) {
//            mOnSeekListener.onSeek(mCurrentPercent);
//        }
    }

    private void getZoomViewBitmap() {
        // 返回一个数字四舍五入后最接近的整数，获取到当前视屏点的毫秒值
        long time = Math.round(mLocalMedia.getDuration() * mCurrentPercent * 1000);

        // TODO 给手指触摸移动的选中view设置显示的图片 手指拖拽 mZoomView 移动的值 bitmap
        mGetFrameBitmapTask = new GetFrameBitmapTask(getContext(), mLocalMedia, false, time, mImageViewHeight, mImageViewHeight, new OnCompleteListenerImpl(mZoomView));
        mGetFrameBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    public class OnSingleBitmapListenerImpl implements GetAllFrameTask.OnSingleBitmapListener {
        private WeakReference<CoverContainer2> mContainerWeakReference;
        private int index;

        public OnSingleBitmapListenerImpl(CoverContainer2 coverContainer) {
            mContainerWeakReference = new WeakReference<>(coverContainer);
        }


        @Override
        public void onSingleBitmapComplete(Bitmap bitmap) {
            CoverContainer2 container = mContainerWeakReference.get();
            if (container != null) {
                container.post(new RunnableImpl(container.mImageViews[index], bitmap));
                index++;
            }
        }

        public class RunnableImpl implements Runnable {
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
                    bitmaps.add(mBitmap);
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
                view.setBitmap(bitmap); // 给手指触摸移动的选中view设置显示的图片
            }
        }
    }

    public void setOnSeekListener(onSeekListener onSeekListener) {
        mOnSeekListener = onSeekListener;
    }

    public interface onSeekListener {
        /**
         *
         * @param percent 当前x轴滑动的百分比
         * @param isStart true 设置当前的seekTo并同时播放，false 只设置当前的seekTo
         */
        void onSeek(float percent, boolean isStart);
        void onSeekEnd();
    }
}
