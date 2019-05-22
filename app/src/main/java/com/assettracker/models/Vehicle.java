package com.assettracker.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Vehicle {
    public String registrationNumber;
    public String carModel;
    public boolean assign;
    public String driverId;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public boolean isAssign() {
        return assign;
    }

    public void setAssign(boolean assign) {
        this.assign = assign;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String ownerId;

    public Vehicle() {
    }

    public Vehicle(String registrationNumber, String carModel, boolean assign, String ownerId) {
        this.registrationNumber = registrationNumber;
        this.carModel = carModel;
        this.assign = assign;
        this.ownerId = ownerId;
    }

    public Vehicle(String registrationNumber, String carModel, boolean assign, String driverId, String ownerId) {
        this.registrationNumber = registrationNumber;
        this.carModel = carModel;
        this.assign = assign;
        this.driverId = driverId;
        this.ownerId = ownerId;
    }
}
