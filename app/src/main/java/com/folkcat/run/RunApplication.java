package com.folkcat.run;

import android.app.Application;


import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Tamas on 2015/10/22.
 */
public class RunApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //配置Realm
        RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(config);
    }
}
