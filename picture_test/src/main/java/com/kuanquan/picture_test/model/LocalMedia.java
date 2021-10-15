package com.kuanquan.picture_test.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LocalMedia implements Parcelable {

    private String path; // original path
    private String coverPath;
    private String realPath; // 真实路径
    private long duration; // 视频时长
    private int width;
    private int height;
    private long size; // 文件大小
    private String fileName; // 文件名称
    private String parentFolderName; // Parent  Folder Name

    public LocalMedia() { }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setParentFolderName(String parentFolderName) {
        this.parentFolderName = parentFolderName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeString(this.coverPath);
        dest.writeString(this.realPath);
        dest.writeLong(this.duration);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.size);
        dest.writeString(this.fileName);
        dest.writeString(this.parentFolderName);
    }

    protected LocalMedia(Parcel in) {
        this.path = in.readString();
        this.coverPath = in.readString();
        this.realPath = in.readString();
        this.duration = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.size = in.readLong();
        this.fileName = in.readString();
        this.parentFolderName = in.readString();
    }

    public static final Creator<LocalMedia> CREATOR = new Creator<LocalMedia>() {
        @Override
        public LocalMedia createFromParcel(Parcel source) {
            return new LocalMedia(source);
        }

        @Override
        public LocalMedia[] newArray(int size) {
            return new LocalMedia[size];
        }
    };
}
