package com.luck.pictureselector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.InstagramSelectionConfig;
import com.luck.picture.lib.instagram.process.InstagramMediaProcessActivity;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.List;

import static com.luck.picture.lib.instagram.InsGallery.THEME_STYLE_DEFAULT;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }

    public static int currentTheme = THEME_STYLE_DEFAULT;
    public void onClick(View view) {
        InsGallery.openGallery(DemoActivity.this, GlideEngine.createGlideEngine(), GlideCacheEngine.createCacheEngine(), new ArrayList<LocalMedia>());
        InsGallery.setCurrentTheme(THEME_STYLE_DEFAULT);
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
        config.style = InsGallery.createInstagramStyle(this);
        config.instagramSelectionConfig = InstagramSelectionConfig.createConfig().setCurrentTheme(currentTheme);
        InstagramMediaProcessActivity.launchActivity(this, config, result, bundle, InstagramMediaProcessActivity.REQUEST_SINGLE_VIDEO_PROCESS);

    }

}