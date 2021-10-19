package com.kuanquan.picture_test.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.kuanquan.picture_test.InstagramMediaProcessActivity;
import com.kuanquan.picture_test.R;
import com.kuanquan.picture_test.callback.LifecycleCallBack;
import com.kuanquan.picture_test.callback.ProcessStateCallBack;
import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.picture_test.task.GetFrameBitmapTask;
import com.kuanquan.picture_test.util.SdkVersionUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * 视频
 * 一进来显示的是封面图片 ImageView 控件，只要下面拖动就开始显示 播放器控件，
 * 且拖动 ZoomView 控件上面切换显示不同的图片其实是播放器设置seekTo，然后停止播放，实现的效果
 */
public class InstagramMediaSingleVideoContainer extends FrameLayout implements ProcessStateCallBack, LifecycleCallBack {
    private FrameLayout mTopContainer;  // 整体布局根 View
    public CoverContainer mCoverView;  // 视频封面图选择器
    private VideoView mVideoView;       // 视频播放控件
    private ImageView mThumbView;       // 封面图
    private MediaPlayer mMediaPlayer;   // 系统播放控制器
    private final LocalMedia mMedia;    // 视频及封面图实体类
    private boolean isStart;  // true 表示可以播放
    private boolean isFirst;  // 是不是第一次进来
    private int mCoverPlayPosition; // 记录封面所在的播放点
    private boolean isPlay;  // true 在播放状态， false 停止播放状态
    private boolean needPause; // 需要暂停标识
    private boolean needSeekCover;
    private Handler handler = new Handler(Looper.getMainLooper());
    int currentPosition; // 当前播放进度
    private int seekDuration; // 拖动的播放点

    public InstagramMediaSingleVideoContainer(@NonNull Context context, LocalMedia media, boolean isAspectRatio) {
        super(context);
        mMedia = media;
        initView(context, media, isAspectRatio);
    }

    int count; // 开始播放到400毫秒就暂停
    boolean autoPlay; // 循环自动播放
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 获得当前播放时间和当前视频的长度
            currentPosition = mVideoView.getCurrentPosition();
            Log.e("监听播放进度", "currentPosition ->" + currentPosition);
            count++;
            if (handler != null) {
                if (count >= 10) {
                    Log.e("wangfei", "播放暂停");
                    startVideo(false);
                    handler.removeCallbacks(runnable);
                    autoPlay = true;
                    startVideo(true);
                    mVideoView.seekTo(seekDuration);
                } else {
                    handler.postDelayed(runnable, 100);
                }
            }
        }
    };

    private void initView(Context context, LocalMedia media, boolean isAspectRatio) {
        View rootView = inflate(context, R.layout.aaa_video, this);
        mTopContainer = rootView.findViewById(R.id.root_view);
        mVideoView = rootView.findViewById(R.id.video_view);
        mVideoView.setVisibility(View.GONE);

        if (SdkVersionUtils.checkedAndroid_Q() && SdkVersionUtils.isContent(media.getPath())) {
            mVideoView.setVideoURI(Uri.parse(media.getPath()));
        } else {
            mVideoView.setVideoPath(media.getPath());
        }

        mVideoView.setOnClickListener((v -> startVideo(!isStart)));

        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mMediaPlayer = mediaPlayer;
            // 监听当前缓存和当前播放进度
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

                int currentPosition, duration;
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    // 获得当前播放时间和当前视频的长度
                    currentPosition = mVideoView.getCurrentPosition();
                    Log.e("监听播放进度", "currentPosition ->" + currentPosition);
                    duration = mVideoView.getDuration();
//                    int progress = ((currentPosition * 100) / duration);
//                    // 设置进度条的主要进度，表示当前的播放时间
//                    SeekBar seekBar = new SeekBar(mVideoView.getContext());
//                    seekBar.setProgress(progress);
//                    // 设置进度条的次要进度，表示视频的缓冲进度
//                    seekBar.setSecondaryProgress(percent);
                }
            });
            mediaPlayer.setLooping(true);
            changeVideoSize(mediaPlayer, isAspectRatio);
            mediaPlayer.setOnInfoListener((mp1, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video started

                    handler.postDelayed(runnable, 100);

                    isPlay = true;
                    if (needSeekCover && mCoverPlayPosition >= 0) {
                        mVideoView.seekTo(mCoverPlayPosition);
                        mCoverPlayPosition = -1;
                        needSeekCover = false;
                    }
                    if (needPause) {
//                        mVideoView.pause();
                        needPause = false;
                    }
                    if (mThumbView.getVisibility() == VISIBLE && !autoPlay) {
                        ObjectAnimator.ofFloat(mThumbView, "alpha", 1.0f, 0).setDuration(400).start();
                    }
                    return true;
                }
                return false;
            });
        });

        mThumbView = rootView.findViewById(R.id.image_view);

        mCoverView = new CoverContainer(context, media);
        addView(mCoverView);

        mCoverView.getFrame(getContext(), media);
        mCoverView.setOnSeekListener(new CoverContainer.onSeekListener() {

            @Override
            public void onSeek(float percent, boolean isStart) {
//                if (!isFirst) {
                if (isStart) {
                    startVideo(true);
                }
                seekDuration = (int) (mMedia.getDuration() * percent);
                mVideoView.seekTo(seekDuration);
            }

            @Override
            public void onSeekEnd() {
                needPause = true;
                autoPlay = false;
                if (isStart && isPlay) {
                    startVideo(false);
                }
            }
        });

        new GetFrameBitmapTask(context, media, isAspectRatio, 0, new OnCompleteListenerImpl(mThumbView)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * @param start true 播放，false 暂停
     */
    private void startVideo(boolean start) {
//        if (isStart == start) {
//            return;
//        }
//        if (isFirst && start) {
//            return;
//        }
        isStart = start;
//        if (!isFirst) {
//            isFirst = true;
            mVideoView.setVisibility(View.VISIBLE);
//        }
        if (!start) {
            mVideoView.pause();
        } else {
            Log.e("wangfei", "开始播放");
            count = 0;
            if (handler != null) {
//                handler.postDelayed(runnable, 100);
            }
            mVideoView.start();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mTopContainer.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        mCoverView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - width, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft = 0;
        mTopContainer.layout(viewLeft, viewTop, viewLeft + mTopContainer.getMeasuredWidth(), viewTop + mTopContainer.getMeasuredHeight());
        viewTop = mTopContainer.getMeasuredHeight();
        mCoverView.layout(viewLeft, viewTop, viewLeft + mCoverView.getMeasuredWidth(), viewTop + mCoverView.getMeasuredHeight());
    }

    @Override
    public void onBack(InstagramMediaProcessActivity activity) {
        activity.finish();
    }

    @Override
    public void onProcess(InstagramMediaProcessActivity activity) {
        int c = 1;
        c++;
        CountDownLatch count = new CountDownLatch(c);
        mCoverView.cropCover(count);
    }

    @Override
    public void onActivityResult(InstagramMediaProcessActivity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onStart(InstagramMediaProcessActivity activity) {

    }

    @Override
    public void onResume(InstagramMediaProcessActivity activity) {
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
        }
        needPause = true;
        needSeekCover = true;
        isStart = false;
    }

    @Override
    public void onPause(InstagramMediaProcessActivity activity) {
        mCoverPlayPosition = mVideoView.getCurrentPosition();
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        isPlay = false;
        needPause = false;
    }

    @Override
    public void onDestroy(InstagramMediaProcessActivity activity) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mVideoView = null;

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        handler = null;
    }

    private float getInstagramAspectRatio(int width, int height) {
        float aspectRatio = 0;
        if (height > width * 1.266f) {
            aspectRatio = width / (width * 1.266f);
        } else if (width > height * 1.9f) {
            aspectRatio = height * 1.9f / height;
        }
        return aspectRatio;
    }

    public void changeVideoSize(MediaPlayer mediaPlayer, boolean isAspectRatio) {
        if (mediaPlayer == null || mVideoView == null) {
            return;
        }
        try {
            mediaPlayer.getVideoWidth();
        } catch (Exception e) {
            return;
        }
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        int parentWidth = getMeasuredWidth();
        int parentHeight = getMeasuredWidth();

        float instagramAspectRatio = getInstagramAspectRatio(videoWidth, videoHeight);
        float targetAspectRatio = videoWidth * 1.0f / videoHeight;

        int height = (int) (parentWidth / targetAspectRatio);

        int adjustWidth;
        int adjustHeight;
        if (isAspectRatio) {
            if (height > parentHeight) {
                adjustWidth = (int) (parentWidth * (instagramAspectRatio > 0 ? instagramAspectRatio : targetAspectRatio));
                adjustHeight = height;
            } else {
                if (instagramAspectRatio > 0) {
                    adjustWidth = (int) (parentHeight * targetAspectRatio);
                    adjustHeight = (int) (parentHeight / instagramAspectRatio);
                } else {
                    adjustWidth = parentWidth;
                    adjustHeight = height;
                }
            }
        } else {
            if (height < parentHeight) {
                adjustWidth = (int) (parentHeight * targetAspectRatio);
                adjustHeight = parentHeight;
            } else {
                adjustWidth = parentWidth;
                adjustHeight = height;
            }
        }

        LayoutParams layoutParams = (LayoutParams) mVideoView.getLayoutParams();
        layoutParams.width = adjustWidth;
        layoutParams.height = adjustHeight;
        mVideoView.setLayoutParams(layoutParams);
    }

    public static class OnCompleteListenerImpl implements GetFrameBitmapTask.OnCompleteListener {
        private final WeakReference<ImageView> mImageViewWeakReference;

        public OnCompleteListenerImpl(ImageView imageView) {
            mImageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        public void onGetBitmapComplete(Bitmap bitmap) {
            ImageView imageView = mImageViewWeakReference.get();
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
