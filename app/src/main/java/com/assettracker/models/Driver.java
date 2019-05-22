package com.assettracker.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver implements Parcelable {
    private String driverName;
    private String driverAge;
    private String mobile;
    private String ownerUID;
    private Boolean assign;
    private String Uid;
    private String carAssigned;
    private Integer Otp;

    public String getUid() {
        return Uid;
    }

    public String getCarAssigned() {
        return carAssigned;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public Driver() {
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverAge(String driverAge) {
        this.driverAge = driverAge;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Driver(String driverName, String driverAge, String mobile,String ownerUID) {
        this.driverName = driverName;
        this.driverAge = driverAge;
        this.mobile = mobile;
        this.ownerUID=ownerUID;
    }

    public Boolean getAssign() {
        return assign;
    }

    public void setAssign(Boolean assign) {
        this.assign = assign;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverAge() {
        return driverAge;
    }

    public String getMobile() {
        return mobile;
    }

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
    }

    public Integer getOtp() {
        return Otp;
    }

    public void setOtp(Integer otp) {
        Otp = otp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.driverName);
        dest.writeString(this.driverAge);
        dest.writeString(this.mobile);
        dest.writeString(this.ownerUID);
        dest.writeValue(this.assign);
        dest.writeString(this.Uid);
        dest.writeString(this.carAssigned);
        dest.writeValue(this.Otp);
    }

    protected Driver(Parcel in) {
        this.driverName = in.readString();
        this.driverAge = in.readString();
        this.mobile = in.readString();
        this.ownerUID = in.readString();
        this.assign = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.Uid = in.readString();
        this.carAssigned = in.readString();
        this.Otp = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel source) {
            return new Driver(source);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };
}
