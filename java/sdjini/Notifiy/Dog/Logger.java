package sdjini.Notify.Dog;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import sdjini.Notify.Setting;

public class Logger {
    private Activity Activity;
    private Context Context;
    private String ClassName;
    private Date Date;
    private Log Log;
    private File LogPath;
    public Logger(Activity Activity) {
        this.Activity = Activity;
        this.Context = this.Activity;
        this.ClassName = this.Activity.getLocalClassName();
        this.Log = new Log(this.Activity, this.ClassName);
        this.LogPath = new File(this.Activity.getFilesDir(),"log.csv");
        try {
            if (!this.LogPath.exists()) this.LogPath.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Logger(Context Context, String ClassName){
        this.Activity = null;
        this.Context = Context;
        this.ClassName = ClassName;
        this.Log = new Log(this.Activity, this.ClassName);
        this.LogPath = new File(this.Context.getFilesDir(),"log.csv");
        try {
            if (!this.LogPath.exists()) this.LogPath.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void write(String Tag, String Message){
        this.Log.setLevel(LogLevel.step);
        this.Log.setLog(Tag, Message);
        step();
    }
    public void write(LogLevel level,String Tag,String Message){
        this.Log.setLevel(level);
        this.Log.setLog(Tag, Message);
        switch (level) {
            case step -> step();
            case debug -> debug();
            case info -> info();
            case error -> error();
            case fatal -> fatal();
            default -> step();
        }
    }
    private void step() {
        if (0 <= LogLevel.step.compareTo(Setting.outputLevel))
            writeLog();
    }
    private void info() {
        if (0 <= LogLevel.info.compareTo(Setting.outputLevel))
            writeLog();
    }
    private void debug() {
        if (0 <= LogLevel.debug.compareTo(Setting.outputLevel))
            writeLog();
    }
    private void error() {
        if (0 <= LogLevel.error.compareTo(Setting.outputLevel))
            writeLog();
    }
    private void fatal()  {
        if (0 <= LogLevel.fatal.compareTo(Setting.outputLevel))
            writeLog();
    }
    private void writeLog() {
            this.Log.updateTime();
            try {
                if (!this.LogPath.exists()) this.LogPath.createNewFile();
                    FileOutputStream fos = new FileOutputStream(LogPath, true);
                fos.write(
                        (String.join(
                                ",",
                                this.Log.Time,
                                this.Log.level.name(),
                                String.format("Activity: %s", this.Log.Activity),
                                String.format("Class: %s", this.Log.Class),
                                this.Log.Tag,
                                this.Log.Message
                        ) + "\n").getBytes()
                );
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
}
