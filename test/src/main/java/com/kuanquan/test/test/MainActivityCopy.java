package com.kuanquan.test.test;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.kuanquan.picture_test.InstagramMediaProcessActivity;
import com.kuanquan.picture_test.PictureSelectionConfig;
import com.kuanquan.picture_test.config.InstagramSelectionConfig;
import com.kuanquan.picture_test.config.PictureConfig;
import com.kuanquan.picture_test.config.PictureParameterStyle;
import com.kuanquan.picture_test.model.LocalMedia;
import com.kuanquan.test.R;

import java.util.ArrayList;
import java.util.List;

//import instagram.InsGallery;
//import static instagram.InsGallery.THEME_STYLE_DEFAULT;

public class MainActivityCopy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
//        InsGallery.openGallery(
//                MainActivity.this,
//                GlideEngine.createGlideEngine(),
//                GlideCacheEngine.createCacheEngine(),
//                new ArrayList<LocalMedia>());

        List<LocalMedia> result = new ArrayList<>();
        LocalMedia localMedia = new LocalMedia();
        localMedia.setFileName("VID_20210929_18262509.mp4");
        localMedia.setDuration(17024);
//        localMedia.setHeight(1200);
//        localMedia.setWidth(1600);
//        localMedia.setId(751);
//        localMedia.setSize(5637854);
        localMedia.setMimeType("video/mp4");
        localMedia.setParentFolderName("Camera");
        localMedia.setPath("/storage/emulated/0/DCIM/Camera/VID_20210929_18262509.mp4");
        localMedia.setRealPath("/storage/emulated/0/DCIM/Camera/VID_20210929_18262509.mp4");
        result.add(localMedia);
        Bundle bundle = new Bundle();
        bundle.putBoolean(InstagramMediaProcessActivity.EXTRA_ASPECT_RATIO, false);
        PictureSelectionConfig config = new PictureSelectionConfig();
        config.isCamera = false;
        config.selectionMode = PictureConfig.SINGLE;
        config.isSingleDirectReturn = true;
        config.isWithVideoImage = false;
        config.maxVideoSelectNum = 1;
        config.aspect_ratio_x = 1;
        config.aspect_ratio_y = 1;
        config.enableCrop = true;
        config.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//        config.style = InsGallery.createInstagramStyle(this);
        config.style = createInstagramStyle(this);
        config.instagramSelectionConfig = InstagramSelectionConfig.createConfig().setCurrentTheme(0);

        InstagramMediaProcessActivity.launchActivity(this, config, result, bundle,
                InstagramMediaProcessActivity.REQUEST_SINGLE_VIDEO_PROCESS);

    }

    public static PictureParameterStyle createInstagramStyle(Context context) {
        // 相册主题
        PictureParameterStyle mPictureParameterStyle = new PictureParameterStyle();
        // 是否改变状态栏字体颜色(黑白切换)
        mPictureParameterStyle.isChangeStatusBarFontColor = true;
        // 是否开启右下角已完成(0/9)风格
        mPictureParameterStyle.isOpenCompletedNumStyle = false;
        // 是否开启类似QQ相册带数字选择风格
        mPictureParameterStyle.isOpenCheckNumStyle = true;
        // 相册状态栏背景色
        mPictureParameterStyle.pictureStatusBarColor = Color.parseColor("#FFFFFF");
        // 相册列表标题栏背景色
        mPictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#FFFFFF");
        // 相册列表标题栏右侧上拉箭头
        mPictureParameterStyle.pictureTitleUpResId = R.drawable.discover_return;
        // 相册列表标题栏右侧下拉箭头
        mPictureParameterStyle.pictureTitleDownResId = R.drawable.discover_return;
        // 相册文件夹列表选中圆点
        mPictureParameterStyle.pictureFolderCheckedDotStyle = R.drawable.picture_orange_oval;
        // 相册返回箭头
        mPictureParameterStyle.pictureLeftBackIcon = R.drawable.discover_return;
        // 标题栏字体颜色
        mPictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(context, R.color.picture_color_black);
        // 相册右侧取消按钮字体颜色  废弃 改用.pictureRightDefaultTextColor和.pictureRightDefaultTextColor
        mPictureParameterStyle.pictureRightDefaultTextColor = ContextCompat.getColor(context, R.color.picture_color_1766FF);
        // 相册父容器背景色
        mPictureParameterStyle.pictureContainerBackgroundColor = ContextCompat.getColor(context, R.color.picture_color_white);
        // 相册列表勾选图片样式
        mPictureParameterStyle.pictureCheckedStyle = R.drawable.discover_return;
        // 相册列表底部背景色
        mPictureParameterStyle.pictureBottomBgColor = ContextCompat.getColor(context, R.color.picture_color_fa);
        // 已选数量圆点背景样式
        mPictureParameterStyle.pictureCheckNumBgStyle = R.drawable.picture_num_oval;
        // 相册列表底下预览文字色值(预览按钮可点击时的色值)
        mPictureParameterStyle.picturePreviewTextColor = ContextCompat.getColor(context, R.color.picture_color_fa632d);
        // 相册列表底下不可预览文字色值(预览按钮不可点击时的色值)
        mPictureParameterStyle.pictureUnPreviewTextColor = ContextCompat.getColor(context, R.color.picture_color_9b);
        // 相册列表已完成色值(已完成 可点击色值)
        mPictureParameterStyle.pictureCompleteTextColor = ContextCompat.getColor(context, R.color.picture_color_fa632d);
        // 相册列表未完成色值(请选择 不可点击色值)
        mPictureParameterStyle.pictureUnCompleteTextColor = ContextCompat.getColor(context, R.color.picture_color_9b);
        // 外部预览界面删除按钮样式
        mPictureParameterStyle.pictureExternalPreviewDeleteStyle = R.drawable.discover_return;
        // 外部预览界面是否显示删除按钮
        mPictureParameterStyle.pictureExternalPreviewGonePreviewDelete = true;
//        // 自定义相册右侧文本内容设置
        mPictureParameterStyle.pictureRightDefaultText = context.getString(R.string.next);
        return mPictureParameterStyle;
    }
}