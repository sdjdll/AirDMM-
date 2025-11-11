package sdjini.AirDMM.Neko.Config;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;

public class Saver {
    private Activity activity;
    private final Context context;
    private final Logger logger;
    public Saver(Activity activity){
        this.activity = activity;
        this.context = this.activity;
        this.logger = new Logger(this.activity, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Saver Initialized");
    }
    public Saver(Context context){
        this.context = context;
        this.logger = new Logger(this.context, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Saver Initialized");
    }
    public void saveString(String PerfName, String Key, String Value){
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(PerfName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Key, Value);
        if(editor.commit()) logger.write(LogLevel.info, "Pref", String.format("%s write done, %s:%s", PerfName, Key, Value));
        else logger.write(LogLevel.error, "Pref", String.format("%s write error", PerfName));
    }
    public void saveInt(String PerfName, String Key, int Value){
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(PerfName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Key, Value);
        editor.apply();
        if(editor.commit()) logger.write(LogLevel.info, "Pref", String.format("%s write done, %s:%d", PerfName, Key, Value));
        else logger.write(LogLevel.error, "Pref", String.format("%s write error", PerfName));
    }
}
