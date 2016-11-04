
package cn.stj.fphealth.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

public class LocationInfo extends DataSupport implements Parcelable {

    private String latitude;
    private String longitude;
    private String time;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public LocationInfo() {
        super();
    }

    public LocationInfo(Parcel in) {
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.time = in.readString();
    }

    public static Parcelable.Creator<LocationInfo> CREATOR = new Creator<LocationInfo>() {
        @Override
        public LocationInfo createFromParcel(Parcel source) {
            return new LocationInfo(source);
        }

        @Override
        public LocationInfo[] newArray(int size) {
            return new LocationInfo[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(time);
    }

    @Override
    public String toString() {
        return "LocationInfo [latitude=" + latitude + ", longitude=" + longitude + ", time=" + time
                + "]";
    }

}
