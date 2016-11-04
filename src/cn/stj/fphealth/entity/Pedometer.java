
package cn.stj.fphealth.entity;

import org.litepal.crud.DataSupport;

public class Pedometer extends DataSupport{

    private int type;
    private String count;
    private String fromTime;
    private String toTime;
    private String height;

    public Pedometer() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Pedometer [type=" + type + ", count=" + count + ", fromTime=" + fromTime
                + ", toTime=" + toTime + ", height=" + height + "]";
    }

}
