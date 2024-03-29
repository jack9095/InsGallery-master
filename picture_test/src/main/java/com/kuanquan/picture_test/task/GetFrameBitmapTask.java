package com.kuanquan.picture_test.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.picture_test.util.ScreenUtils;
import com.kuanquan.picture_test.util.SdkVersionUtils;

import java.io.File;
import java.lang.ref.WeakReference;

public class GetFrameBitmapTask extends AsyncTask<Void, Void, Bitmap> {
    private WeakReference<Context> mContextWeakReference;
    private LocalMedia mMedia;
    private OnCompleteListener mOnCompleteListener;
    private boolean isAspectRatio;
    private long mTime;
    private int mCropWidth;
    private int mCropHeight;

    public GetFrameBitmapTask(Context context, LocalMedia media, boolean isAspectRatio, long time, OnCompleteListener onCompleteListener) {
        mContextWeakReference = new WeakReference<>(context);
        mMedia = media;
        this.isAspectRatio = isAspectRatio;
        mTime = time;
        mOnCompleteListener = onCompleteListener;
    }

    public GetFrameBitmapTask(Context context, LocalMedia media, boolean isAspectRatio, long time, int cropWidth, int cropHeight, OnCompleteListener onCompleteListener) {
        this(context, media, isAspectRatio, time, onCompleteListener);
        mCropWidth = cropWidth;
        mCropHeight = cropHeight;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        Context context = mContextWeakReference.get();
        if (context != null) {
            try {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                Uri uri;
                if (SdkVersionUtils.checkedAndroid_Q() && SdkVersionUtils.isContent(mMedia.getPath())) {
                    uri = Uri.parse(mMedia.getPath());
                } else {
                    uri = Uri.fromFile(new File(mMedia.getPath()));
                }
                mediaMetadataRetriever.setDataSource(context, uri);
                Bitmap frame = mediaMetadataRetriever.getFrameAtTime(mTime);

                if (isAspectRatio) {
                    int width = frame.getWidth();
                    int height = frame.getHeight();
                    float instagramAspectRatio = getInstagramAspectRatio(width, height);
                    float targetAspectRatio = instagramAspectRatio > 0 ? instagramAspectRatio : width * 1.0f / height;
                    int adjustWidth;
                    int adjustHeight;

                    float resizeScale;
                    int cropOffsetX = 0;
                    int cropOffsetY = 0;
                    if (height > width) {
                        adjustHeight = ScreenUtils.getScreenWidth(context);
                        adjustWidth = (int) (adjustHeight * targetAspectRatio);
                        if (instagramAspectRatio > 0) {
                            resizeScale = adjustWidth * 1.0f / width;
                            cropOffsetY = (int) ((height * resizeScale - adjustHeight) / 2);
                        } else {
                            resizeScale = adjustHeight * 1.0f / height;
                        }
                    } else {
                        adjustWidth = ScreenUtils.getScreenWidth(context);
                        adjustHeight = (int) (adjustWidth / targetAspectRatio);
                        if (instagramAspectRatio > 0) {
                            resizeScale = adjustHeight * 1.0f / height;
                            cropOffsetX = (int) ((width * resizeScale - adjustWidth) / 2);
                        } else {
                            resizeScale = adjustWidth * 1.0f / width;
                        }
                    }

                    frame = Bitmap.createScaledBitmap(frame,
                            Math.round(width * resizeScale),
                            Math.round(height * resizeScale), false);

                    frame = Bitmap.createBitmap(frame, cropOffsetX, cropOffsetY, adjustWidth, adjustHeight);
                } else {

                    if (mCropWidth > 0 && mCropHeight > 0) {
                        float scale;
                        if (frame.getWidth() > frame.getHeight()) {
                            scale = mCropHeight * 1f / frame.getHeight();
                        } else {
                            scale = mCropWidth * 1f / frame.getWidth();
                        }

                        frame = Bitmap.createScaledBitmap(frame,
                                Math.round(frame.getWidth() * scale),
                                Math.round(frame.getHeight() * scale), false);
                    }

                    int cropWidth = Math.min(frame.getWidth(), frame.getHeight());
                    int cropOffsetX = (frame.getWidth() - cropWidth) / 2;
                    int cropOffsetY = (frame.getHeight() - cropWidth) / 2;
                    // TODO 获取滑动到某处的帧图片 width = cropWidth / 2
                    frame = Bitmap.createBitmap(frame, cropOffsetX, cropOffsetY, cropWidth, cropWidth);
                }
                mediaMetadataRetriever.release();
                return frame;
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mOnCompleteListener != null) {
            mOnCompleteListener.onGetBitmapComplete(bitmap);
        }
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

    public interface OnCompleteListener {
        void onGetBitmapComplete(Bitmap bitmap);
    }
}
