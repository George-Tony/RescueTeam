package com.webcrs.rescueteam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG =  NetworkChangeReceiver.class.getSimpleName();
    private Realm mRealm;
    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable() || mobile.isAvailable()) {

            // update location
            Realm.init(context);
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);

            mRealm.beginTransaction();
            RealmResults<UpdateLocationModel> results = mRealm.where(UpdateLocationModel.class).findAll();
            Log.e(TAG, "Network Available !!"+ results.last());
        }
    }
}
