package com.webcrs.rescueteam;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class GlobalApplication extends Application {

    private static GlobalApplication mInstance;
    public static synchronized GlobalApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("RescueTeam.realm").build();
        Realm.setDefaultConfiguration(config);
    }



}
