package com.kuanquan.picture_test.model;


import android.os.Parcel;
import android.os.Parcelable;
import com.kuanquan.picture_test.config.PictureConfig;


public class LocalMedia implements Parcelable {
    /**
     * file to ID
     */
    private long id;
    /**
     * original path
     */
    private String path;

    private String coverPath;

    /**
     * The real path，But you can't get access from AndroidQ
     * <p>
     * It could be empty
     * <p/>
     */
    private String realPath;

    /**
     * # Check the original button to get the return value
     * original path
     */
    private String originalPath;
    /**
     * compress path
     */
    private String compressPath;
    /**
     * cut path
     */
    private String cutPath;

    /**
     * Note: this field is only returned in Android Q version
     * <p>
     * Android Q image or video path
     */
    private String androidQToPath;
    /**
     * video duration
     */
    private long duration;
    /**
     * If the selected
     * # Internal use
     */
    private boolean isChecked;
    /**
     * If the cut
     */
    private boolean isCut;
    /**
     * media position of list
     */
    public int position;


    /**
     * If the compressed
     */
    private boolean compressed;
    /**
     * image or video width
     * <p>
     * # If zero occurs, the developer needs to handle it extra
     */
    private int width;
    /**
     * image or video height
     * <p>
     * # If zero occurs, the developer needs to handle it extra
     */
    private int height;

    /**
     * file size
     */
    private long size;

    /**
     * Whether the original image is displayed
     */
    private boolean isOriginal;

    /**
     * file name
     */
    private String fileName;

    /**
     * Parent  Folder Name
     */
    private String parentFolderName;

    /**
     * orientation info
     * # For internal use only
     */
    private int orientation = -1;

    /**
     * loadLongImageStatus
     * # For internal use only
     */
    public int loadLongImageStatus = PictureConfig.NORMAL;

    /**
     * bucketId
     */
    private long bucketId = -1;


    public LocalMedia() {

    }

    public LocalMedia(String path, long duration, int chooseModel, String mimeType) {
        this.path = path;
        this.duration = duration;
    }

    public LocalMedia(long id, String path, String fileName, String parentFolderName, long duration, int chooseModel,
                      String mimeType, int width, int height, long size) {
        this.id = id;
        this.path = path;
        this.fileName = fileName;
        this.parentFolderName = parentFolderName;
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.size = size;
    }

    public LocalMedia(long id, String path, String absolutePath, String fileName, String parentFolderName, long duration, int chooseModel,
                      String mimeType, int width, int height, long size, long bucketId) {
        this.id = id;
        this.path = path;
        this.realPath = absolutePath;
        this.fileName = fileName;
        this.parentFolderName = parentFolderName;
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.size = size;
        this.bucketId = bucketId;
    }

    public LocalMedia(String path, long duration,
                      boolean isChecked, int position, int num, int chooseModel) {
        this.path = path;
        this.duration = duration;
        this.isChecked = isChecked;
        this.position = position;
    }

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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setParentFolderName(String parentFolderName) {
        this.parentFolderName = parentFolderName;
    }

    @Override
    public String toString() {
        return "LocalMedia{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", cutPath='" + cutPath + '\'' +
                ", androidQToPath='" + androidQToPath + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", size=" + size +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.path);
        dest.writeString(this.coverPath);
        dest.writeString(this.realPath);
        dest.writeString(this.originalPath);
        dest.writeString(this.compressPath);
        dest.writeString(this.cutPath);
        dest.writeString(this.androidQToPath);
        dest.writeLong(this.duration);
        dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isCut ? (byte) 1 : (byte) 0);
        dest.writeInt(this.position);
        dest.writeByte(this.compressed ? (byte) 1 : (byte) 0);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.size);
        dest.writeByte(this.isOriginal ? (byte) 1 : (byte) 0);
        dest.writeString(this.fileName);
        dest.writeString(this.parentFolderName);
        dest.writeInt(this.orientation);
        dest.writeInt(this.loadLongImageStatus);
        dest.writeLong(this.bucketId);
    }

    protected LocalMedia(Parcel in) {
        this.id = in.readLong();
        this.path = in.readString();
        this.coverPath = in.readString();
        this.realPath = in.readString();
        this.originalPath = in.readString();
        this.compressPath = in.readString();
        this.cutPath = in.readString();
        this.androidQToPath = in.readString();
        this.duration = in.readLong();
        this.isChecked = in.readByte() != 0;
        this.isCut = in.readByte() != 0;
        this.position = in.readInt();
        this.compressed = in.readByte() != 0;
        this.width = in.readInt();
        this.height = in.readInt();
        this.size = in.readLong();
        this.isOriginal = in.readByte() != 0;
        this.fileName = in.readString();
        this.parentFolderName = in.readString();
        this.orientation = in.readInt();
        this.loadLongImageStatus = in.readInt();
        this.bucketId = in.readLong();
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
