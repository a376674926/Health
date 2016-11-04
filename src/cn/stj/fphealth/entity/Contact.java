package cn.stj.fphealth.entity;

public class Contact {

    private int userId;
    private String nickName;
    private String phoneNumber;
    private String headImage;
    private int isSos;
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getHeadImage() {
        return headImage;
    }
    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }
    public int getIsSos() {
        return isSos;
    }
    public void setIsSos(int isSos) {
        this.isSos = isSos;
    }
    
}
