package com.titans.grouptravelplanner;

public interface FirestoreDbOperationCallback {

    void onSuccess(Object object);
    void onFailure(Object object);
}
