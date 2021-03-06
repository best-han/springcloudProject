package com.windaka.suizhi.webapi.model;

import java.util.Date;

public class CaptureAlarmCar {
    private Integer id;

    private String communityCode;

    private Date captureTime;

    private String deviceCode;

    private String deviceName;

    private Short deviceType;

    private String deviceTypeName;

    private String deviceLocation;

    private String capImage;

    private String capSmallImage;

    private String capVideo;

    private String carNum;

    private String detailId;

    private Integer captureId;

    private Short detailLevel;

    private String detailLevelName;

    private String detailReason;

    private String groupCode;

    private String groupName;

    private Byte handelStatus;

    private Date handelTime;

    private String handelUser;

    private String handelUserId;

    private String handelConn;

    private String createUser;

    private String createUserId;

    private Date createTime;

    private String updateUser;

    private String updateUserId;

    private Date updaetTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommunityCode() {
        return communityCode;
    }

    public void setCommunityCode(String communityCode) {
        this.communityCode = communityCode == null ? null : communityCode.trim();
    }

    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode == null ? null : deviceCode.trim();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName == null ? null : deviceName.trim();
    }

    public Short getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Short deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName == null ? null : deviceTypeName.trim();
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation == null ? null : deviceLocation.trim();
    }

    public String getCapImage() {
        return capImage;
    }

    public void setCapImage(String capImage) {
        this.capImage = capImage == null ? null : capImage.trim();
    }

    public String getCapSmallImage() {
        return capSmallImage;
    }

    public void setCapSmallImage(String capSmallImage) {
        this.capSmallImage = capSmallImage == null ? null : capSmallImage.trim();
    }

    public String getCapVideo() {
        return capVideo;
    }

    public void setCapVideo(String capVideo) {
        this.capVideo = capVideo == null ? null : capVideo.trim();
    }

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum == null ? null : carNum.trim();
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId == null ? null : detailId.trim();
    }

    public Integer getCaptureId() {
        return captureId;
    }

    public void setCaptureId(Integer captureId) {
        this.captureId = captureId;
    }

    public Short getDetailLevel() {
        return detailLevel;
    }

    public void setDetailLevel(Short detailLevel) {
        this.detailLevel = detailLevel;
    }

    public String getDetailLevelName() {
        return detailLevelName;
    }

    public void setDetailLevelName(String detailLevelName) {
        this.detailLevelName = detailLevelName == null ? null : detailLevelName.trim();
    }

    public String getDetailReason() {
        return detailReason;
    }

    public void setDetailReason(String detailReason) {
        this.detailReason = detailReason == null ? null : detailReason.trim();
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode == null ? null : groupCode.trim();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public Byte getHandelStatus() {
        return handelStatus;
    }

    public void setHandelStatus(Byte handelStatus) {
        this.handelStatus = handelStatus;
    }

    public Date getHandelTime() {
        return handelTime;
    }

    public void setHandelTime(Date handelTime) {
        this.handelTime = handelTime;
    }

    public String getHandelUser() {
        return handelUser;
    }

    public void setHandelUser(String handelUser) {
        this.handelUser = handelUser == null ? null : handelUser.trim();
    }

    public String getHandelUserId() {
        return handelUserId;
    }

    public void setHandelUserId(String handelUserId) {
        this.handelUserId = handelUserId == null ? null : handelUserId.trim();
    }

    public String getHandelConn() {
        return handelConn;
    }

    public void setHandelConn(String handelConn) {
        this.handelConn = handelConn == null ? null : handelConn.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId == null ? null : createUserId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId == null ? null : updateUserId.trim();
    }

    public Date getUpdaetTime() {
        return updaetTime;
    }

    public void setUpdaetTime(Date updaetTime) {
        this.updaetTime = updaetTime;
    }
}