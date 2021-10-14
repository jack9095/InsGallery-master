package com.kuanquan.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.InstagramSelectionConfig;
import com.luck.picture.lib.instagram.process.InstagramMediaProcessActivity;

import java.util.ArrayList;
import java.util.List;

import static com.luck.picture.lib.instagram.InsGallery.THEME_STYLE_DEFAULT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        InsGallery.openGallery(
                MainActivity.this,
                GlideEngine.createGlideEngine(),
                GlideCacheEngine.createCacheEngine(),
                new ArrayList<LocalMedia>());
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
        config.instagramSelectionConfig = InstagramSelectionConfig.createConfig().setCurrentTheme(THEME_STYLE_DEFAULT);

        launchActivity(this, config, result, bundle,
                        InstagramMediaProcessActivity.REQUEST_SINGLE_VIDEO_PROCESS);

    }

    public void launchActivity(Activity activity, PictureSelectionConfig config, List<LocalMedia> images, Bundle extras, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), InstagramMediaProcessActivity.class);
        intent.putExtra(PictureConfig.EXTRA_CONFIG, config);
        intent.putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST,
                (ArrayList<? extends Parcelable>) images);
        if (extras != null) {
            intent.putExtras(extras);
        }
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(0, 0);
    }
}