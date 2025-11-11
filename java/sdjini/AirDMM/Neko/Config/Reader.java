package sdjini.AirDMM.Neko.Config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import sdjini.AirDMM.Dog.Logger;

public class Reader {
    private SharedPreferences sharedPreferences;
    private final Context context;
    private Logger logger;
    public Reader(Activity activity, String sharedPreferencesName){
        this.context = activity;
        logger = new Logger(this.context, this.getClass().getName());
        logger.write("Initialize", "Reader Initialized");
        sharedPreferences = this.context.getSharedPreferences(sharedPreferencesName, android.content.Context.MODE_PRIVATE);
    }

    public Reader(Context context, String sharedPreferencesName){
        this.context = context;
        logger = new Logger(this.context, this.getClass().getName());
        logger.write("SharedPreferences", "Initialize Shared Preferences");
        sharedPreferences = this.context.getSharedPreferences(sharedPreferencesName, android.content.Context.MODE_PRIVATE);
    }

    public String getString(String key){
        logger.write("SharedPreferences", "get Shared Preferences");
        return sharedPreferences.getString(key, "");
    }
    public int getInt(String key){
        logger.write("SharedPreferences", "get Shared Preferences");
        return sharedPreferences.getInt(key, 0);
    }
}
