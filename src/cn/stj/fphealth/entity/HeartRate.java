
package cn.stj.fphealth.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * @author hhj@20160804
 */
public class HeartRate extends DataSupport implements Parcelable {

    private int id;
    private String high;
    private String low;
    private String rate;
    private String fromTime;
    private String toTime;

    public HeartRate() {
        super();
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HeartRate(Parcel in) {
        this.id = in.readInt();
        this.high = in.readString();
        this.low = in.readString();
        this.fromTime = in.readString();
        this.toTime = in.readString();
        this.rate = in.readString();
    }

    public static Parcelable.Creator<HeartRate> CREATOR = new Creator<HeartRate>() {
        @Override
        public HeartRate createFromParcel(Parcel source) {
            return new HeartRate(source);
        }

        @Override
        public HeartRate[] newArray(int size) {
            return new HeartRate[size];
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
        dest.writeString(high);
        dest.writeString(low);
        dest.writeString(fromTime);
        dest.writeString(toTime);
        dest.writeString(rate);
    }

}
