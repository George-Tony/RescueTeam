package com.webcrs.rescueteam;

import io.realm.RealmObject;

public class HelpNeededListModel extends RealmObject{

    String modelVarsosNeeded, modelVarcontactNumbers, modelVaralternativeNumbers,
            modelVardescription, modelVardateTime, latitude, longitude, status;

    public String getModelVarsosNeeded() {
        return modelVarsosNeeded;
    }

    public void setModelVarsosNeeded(String modelVarsosNeeded) {
        this.modelVarsosNeeded = modelVarsosNeeded;
    }

    public String getModelVarcontactNumbers() {
        return modelVarcontactNumbers;
    }

    public void setModelVarcontactNumbers(String modelVarcontactNumbers) {
        this.modelVarcontactNumbers = modelVarcontactNumbers;
    }

    public String getModelVaralternativeNumbers() {
        return modelVaralternativeNumbers;
    }

    public void setModelVaralternativeNumbers(String modelVaralternativeNumbers) {
        this.modelVaralternativeNumbers = modelVaralternativeNumbers;
    }

    public String getModelVardescription() {
        return modelVardescription;
    }

    public void setModelVardescription(String modelVardescription) {
        this.modelVardescription = modelVardescription;
    }

    public String getModelVardateTime() {
        return modelVardateTime;
    }

    public void setModelVardateTime(String modelVardateTime) {
        this.modelVardateTime = modelVardateTime;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
