package com.example.manufacturehome;

import android.app.Application;
import android.content.Context;

//用于获取全局上下文context
public class BaseApplication extends Application {
    private static Context context;
    public void onCreate(){
        super.onCreate();
        BaseApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return BaseApplication.context;
    }
}
