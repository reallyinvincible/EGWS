package com.exuberant.egws.models;

import java.util.List;

public class Report {

    String authorId;
    String authorEmail;
    String reportId;
    double latitude;
    double longitude;
    String date;
    String time;
    String buildingName;
    String buildingAddress;
    String buildingCity;
    String pinCode;
    String buildingEmail;
    String firstPhoneNumber;
    String secondPhoneNumber;
    String type;
    String nameBoardPhoto;
    String buildingPhoto;

    public Report() {
    }

    public Report(String authorId, String authorEmail, String reportId, double latitude, double longitude, String date, String time, String buildingName, String buildingAddress, String buildingCity, String pinCode, String buildingEmail, String firstPhoneNumber, String secondPhoneNumber, String type, String nameBoardPhoto, String buildingPhoto) {
        this.authorId = authorId;
        this.authorEmail = authorEmail;
        this.reportId = reportId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.time = time;
        this.buildingName = buildingName;
        this.buildingAddress = buildingAddress;
        this.buildingCity = buildingCity;
        this.pinCode = pinCode;
        this.buildingEmail = buildingEmail;
        this.firstPhoneNumber = firstPhoneNumber;
        this.secondPhoneNumber = secondPhoneNumber;
        this.type = type;
        this.nameBoardPhoto = nameBoardPhoto;
        this.buildingPhoto = buildingPhoto;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingAddress() {
        return buildingAddress;
    }

    public void setBuildingAddress(String buildingAddress) {
        this.buildingAddress = buildingAddress;
    }

    public String getBuildingCity() {
        return buildingCity;
    }

    public void setBuildingCity(String buildingCity) {
        this.buildingCity = buildingCity;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getBuildingEmail() {
        return buildingEmail;
    }

    public void setBuildingEmail(String buildingEmail) {
        this.buildingEmail = buildingEmail;
    }

    public String getFirstPhoneNumber() {
        return firstPhoneNumber;
    }

    public void setFirstPhoneNumber(String firstPhoneNumber) {
        this.firstPhoneNumber = firstPhoneNumber;
    }

    public String getSecondPhoneNumber() {
        return secondPhoneNumber;
    }

    public void setSecondPhoneNumber(String secondPhoneNumber) {
        this.secondPhoneNumber = secondPhoneNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameBoardPhoto() {
        return nameBoardPhoto;
    }

    public void setNameBoardPhoto(String nameBoardPhoto) {
        this.nameBoardPhoto = nameBoardPhoto;
    }

    public String getBuildingPhoto() {
        return buildingPhoto;
    }

    public void setBuildingPhoto(String buildingPhoto) {
        this.buildingPhoto = buildingPhoto;
    }
}
