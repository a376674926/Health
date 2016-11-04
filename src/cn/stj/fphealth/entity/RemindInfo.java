package cn.stj.fphealth.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import org.litepal.crud.DataSupport;

public class RemindInfo extends DataSupport implements Parcelable{
    
    private int id;
    private int remindId;
    private String type;//(type:image 图片 audio 语音 video 视频 html 网页 txt 文本)
    private String content;
    private String name;
    private int periodType;//（提醒周期：1.一次性，2. 日 3.工作日 4.周 5.月 6.年）
    private long time;
    private long endTime;
    private int userId;
    public static final int PERIOD_ONCE = 1;
    public static final int PERIOD_DAY = 2;
    public static final int PERIOD_WORKING_DAY = 3;
    public static final int PERIOD_WEEK = 4;
    public static final int PERIOD_MONTH = 5;
    public static final int PERIOD_YEAR = 6;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getRemindId() {
        return remindId;
    }
    public void setRemindId(int remindId) {
        this.remindId = remindId;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getPeriodType() {
        return periodType;
    }
    public void setPeriodType(int periodType) {
        this.periodType = periodType;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public RemindInfo() {
        super();
    }
    public RemindInfo(Parcel in) {
        this.id = in.readInt();
        this.remindId = in.readInt();
        this.type = in.readString();
        this.content = in.readString();
        this.name = in.readString();
        this.periodType = in.readInt();
        this.time = in.readLong();
        this.endTime = in.readLong();
        this.userId = in.readInt();
    }

    public static Parcelable.Creator<RemindInfo> CREATOR = new Creator<RemindInfo>() {
        @Override
        public RemindInfo createFromParcel(Parcel source) {
            return new RemindInfo(source);
        }

        @Override
        public RemindInfo[] newArray(int size) {
            return new RemindInfo[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(remindId);
        dest.writeString(type);
        dest.writeString(content);
        dest.writeString(name);
        dest.writeInt(periodType);
        dest.writeLong(time);
        dest.writeLong(endTime);
        dest.writeInt(userId);
    }
    @Override
    public String toString() {
        return "RemindInfo [id=" + id + ", remindId=" + remindId + ", type=" + type + ", content="
                + content + ", name=" + name + ", periodType=" + periodType + ", time=" + time
                + ", endTime=" + endTime + ", userId=" + userId + "]";
    }
    

}
