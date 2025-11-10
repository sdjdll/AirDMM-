package sdjini.Notifiy.Dog;

import android.app.Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import sdjini.Notifiy.Setting;

public class Logger {
    private Activity Activity;
    private String ClassName;
    private Date Date;
    private Log Log;
    private File LogPath;
    public Logger(Activity Activity) {
        this.Activity = Activity;
        this.ClassName = this.Activity.getLocalClassName();
        this.Log = new Log(this.Activity, this.ClassName);
        this.LogPath = new File(this.Activity.getFilesDir(),"log.csv");
        try {
            if (!this.LogPath.exists()) this.LogPath.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Logger(Activity Activity,String ClassName){
        this.Activity = Activity;
        this.ClassName = ClassName;
        this.Log = new Log(this.Activity, this.ClassName);
    }
    public void write(String Tag, String Message){
        this.Log.setLevel(LogLevel.step);
        this.Log.setLog(Tag, Message);
        step();
    }
    public void write(LogLevel level,String Tag,String Message){
        this.Log.setLevel(LogLevel.step);
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
        if (0 >= Setting.outputLevel.compareTo(LogLevel.step))
            writeLog();
    }
    private void info() {
        if (0 >= Setting.outputLevel.compareTo(LogLevel.info))
            writeLog();
    }
    private void debug() {
        if (0 >= Setting.outputLevel.compareTo(LogLevel.debug))
            writeLog();
    }
    private void error() {
        if (0 >= Setting.outputLevel.compareTo(LogLevel.error))
            writeLog();
    }
    private void fatal()  {
        if (0 >= Setting.outputLevel.compareTo(LogLevel.fatal))
            writeLog();
    }
    private void writeLog() {
        new Thread(() -> {
            try{
                FileOutputStream fos = new FileOutputStream(LogPath);
                fos.write(
                        String.join(
                                ",",
                                this.Log.Time,
                                this.Log.level.name(),
                                String.format("Activity: %s", this.Log.Activity),
                                String.format("Class: %s", this.Log.Class),
                                this.Log.Tag,
                                this.Log.Message
                        ).getBytes()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
