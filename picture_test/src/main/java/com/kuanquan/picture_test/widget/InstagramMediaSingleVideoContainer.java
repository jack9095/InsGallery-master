package com.kuanquan.picture_test.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.kuanquan.picture_test.InstagramMediaProcessActivity;
import com.kuanquan.picture_test.PictureSelectionConfig;
import com.kuanquan.picture_test.R;
import com.kuanquan.picture_test.callback.LifecycleCallBack;
import com.kuanquan.picture_test.callback.ProcessStateCallBack;
import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.picture_test.task.getFrameBitmapTask;
import com.kuanquan.picture_test.util.AnimatorListenerImpl;
import com.kuanquan.picture_test.util.SdkVersionUtils;
import com.kuanquan.picture_test.util.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * 视频
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 */
public class InstagramMediaSingleVideoContainer extends FrameLayout implements ProcessStateCallBack, LifecycleCallBack {
    private FrameLayout mTopContainer;
    private CoverContainer mCoverView;
    private VideoView mVideoView;
    private ImageView mThumbView;
    private ImageView mPlayButton;
    private MediaPlayer mMediaPlayer;
    private PictureSelectionConfig mConfig;
    private LocalMedia mMedia;
    private boolean isStart;
    private ObjectAnimator mPlayAnimator;
    private boolean isFrist;
    private boolean isVolumeOff;
    private int mCoverPlayPosition;
    private boolean isPlay;
    private boolean needPause;
    private boolean needSeekCover;

    public InstagramMediaSingleVideoContainer(@NonNull Context context) {
        super(context);
    }

    public InstagramMediaSingleVideoContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InstagramMediaSingleVideoContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InstagramMediaSingleVideoContainer(@NonNull Context context, PictureSelectionConfig config, LocalMedia media, boolean isAspectRatio) {
        super(context);
        mConfig = config;
        mMedia = media;
        init(context, media, isAspectRatio);
    }

    private void init(Context context, LocalMedia media, boolean isAspectRatio) {
        View rootView = inflate(context, R.layout.aaa_video,this);
        mTopContainer = rootView.findViewById(R.id.root_view);
        mVideoView = rootView.findViewById(R.id.video_view);
        mVideoView.setVisibility(View.GONE);

        if (SdkVersionUtils.checkedAndroid_Q() && SdkVersionUtils.isContent(media.getPath())) {
            mVideoView.setVideoURI(Uri.parse(media.getPath()));
        } else {
            mVideoView.setVideoPath(media.getPath());
        }

        mVideoView.setOnClickListener((v -> startVideo(!isStart)));

        mVideoView.setOnPreparedListener(mp -> {
            mMediaPlayer = mp;
            setVolume(isVolumeOff);
            mp.setLooping(true);
            changeVideoSize(mp, isAspectRatio);
            mp.setOnInfoListener((mp1, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video started
                    isPlay = true;
                    if (needSeekCover && mCoverPlayPosition >= 0) {
                        mVideoView.seekTo(mCoverPlayPosition);
                        mCoverPlayPosition = -1;
                        needSeekCover = false;
                    }
                    if (needPause) {
                        mVideoView.pause();
                        needPause = false;
                    }
                    if (mThumbView.getVisibility() == VISIBLE) {
                        ObjectAnimator.ofFloat(mThumbView, "alpha", 1.0f, 0).setDuration(400).start();
                    }
                    return true;
                }
                return false;
            });
        });

        mThumbView = rootView.findViewById(R.id.image_view);

        mPlayButton = rootView.findViewById(R.id.play_view);
        mPlayButton.setOnClickListener(v -> startVideo(!isStart));

        mCoverView = new CoverContainer(context, media);
        addView(mCoverView);

//        mCoverView = rootView.findViewById(R.id.cover_view);
        mCoverView.getFrame(getContext(), media);
        mCoverView.setOnSeekListener(new CoverContainer.onSeekListener() {
            @Override
            public void onSeek(float percent) {
                if (!isFrist) {
                    startVideo(true);
                }
                mVideoView.seekTo((int) (mMedia.getDuration() * percent));
            }

            @Override
            public void onSeekEnd() {
                needPause = true;
                if (isStart && isPlay) {
                    startVideo(false);
                }
                mPlayButton.setVisibility(GONE);
            }
        });

        new getFrameBitmapTask(context, media, isAspectRatio, -1, new OnCompleteListenerImpl(mThumbView)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     *
     * @param start true 播放，false 暂停
     */
    private void startVideo(boolean start) {
        if (isStart == start) {
            return;
        }

        if (isFrist && start) {
            return;
        }

        isStart = start;

        if (!isFrist) {
            isFrist = true;
            mVideoView.setVisibility(View.VISIBLE);
        }

        if (mPlayAnimator != null && mPlayAnimator.isRunning()) {
            mPlayAnimator.cancel();
        }
        if (!start) {
            mPlayButton.setVisibility(VISIBLE);
            mPlayAnimator = ObjectAnimator.ofFloat(mPlayButton, "alpha", 0, 1.0f).setDuration(200);
            mVideoView.pause();
        } else {
            mPlayAnimator = ObjectAnimator.ofFloat(mPlayButton, "alpha", 1.0f, 0).setDuration(200);
            mPlayAnimator.addListener(new AnimatorListenerImpl() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPlayButton.setVisibility(GONE);
                }
            });
            mVideoView.start();
        }
        mPlayAnimator.start();
    }

    private void offVolume(ImageView view, boolean off) {
        if (isVolumeOff == off) {
            return;
        }
        isVolumeOff = off;
        if (off) {
            view.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.picture_color_1766FF), PorterDuff.Mode.MULTIPLY));
            ToastUtils.s(getContext(), getContext().getString(R.string.video_sound_off));
        } else {
                view.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.picture_color_black), PorterDuff.Mode.MULTIPLY));
                view.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
            ToastUtils.s(getContext(), getContext().getString(R.string.video_sound_on));
        }
        if (mMediaPlayer != null) {
            setVolume(off);
        }
    }

    private void setVolume(boolean off) {
        if (off) {
            mMediaPlayer.setVolume(0f, 0f);
        } else {
            mMediaPlayer.setVolume(1f, 1f);
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
        activity.setResult(InstagramMediaProcessActivity.RESULT_MEDIA_PROCESS_CANCELED);
        activity.finish();
    }

    @Override
    public void onCenterFeature(InstagramMediaProcessActivity activity, ImageView view) {
        offVolume(view, !isVolumeOff);
    }

    @Override
    public void onProcess(InstagramMediaProcessActivity activity) {
        int c = 1;
        if (mConfig.instagramSelectionConfig.haveCover()) {
            c++;
        }
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

    public static class OnCompleteListenerImpl implements getFrameBitmapTask.OnCompleteListener {
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
