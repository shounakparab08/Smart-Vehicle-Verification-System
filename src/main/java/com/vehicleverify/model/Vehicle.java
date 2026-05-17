package com.vehicleverify.model;

public class Vehicle {
    private String vehicleNo;
    private String ownerName;
    private String insuranceStatus;
    private String vehicleType;
    private String vehicleModel;
    private String registrationDate;
    private boolean blacklistStatus;

    public Vehicle() {}

    public Vehicle(String vehicleNo, String ownerName, String insuranceStatus, String vehicleType, String registrationDate, boolean blacklistStatus) {
        this(vehicleNo, ownerName, insuranceStatus, vehicleType, "N/A", registrationDate, blacklistStatus);
    }

    public Vehicle(String vehicleNo, String ownerName, String insuranceStatus, String vehicleType, String vehicleModel, String registrationDate, boolean blacklistStatus) {
        this.vehicleNo = vehicleNo;
        this.ownerName = ownerName;
        this.insuranceStatus = insuranceStatus;
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
        this.registrationDate = registrationDate;
        this.blacklistStatus = blacklistStatus;
    }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getInsuranceStatus() { return insuranceStatus; }
    public void setInsuranceStatus(String insuranceStatus) { this.insuranceStatus = insuranceStatus; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public boolean isBlacklistStatus() { return blacklistStatus; }
    public void setBlacklistStatus(boolean blacklistStatus) { this.blacklistStatus = blacklistStatus; }
}
