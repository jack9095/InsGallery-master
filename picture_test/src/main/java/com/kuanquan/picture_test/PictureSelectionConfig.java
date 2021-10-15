package com.kuanquan.picture_test;

import android.os.Parcel;
import android.os.Parcelable;

public final class PictureSelectionConfig implements Parcelable {

    public int requestedOrientation;
    public boolean zoomAnim;

    public static PictureSelectionConfig getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final class InstanceHolder {
        private static final PictureSelectionConfig INSTANCE = new PictureSelectionConfig();
    }

    public PictureSelectionConfig() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestedOrientation);
        dest.writeByte(this.zoomAnim ? (byte) 1 : (byte) 0);
    }

    protected PictureSelectionConfig(Parcel in) {
        this.requestedOrientation = in.readInt();
        this.zoomAnim = in.readByte() != 0;
    }

    public static final Creator<PictureSelectionConfig> CREATOR = new Creator<PictureSelectionConfig>() {
        @Override
        public PictureSelectionConfig createFromParcel(Parcel source) {
            return new PictureSelectionConfig(source);
        }

        @Override
        public PictureSelectionConfig[] newArray(int size) {
            return new PictureSelectionConfig[size];
        }
    };
}
