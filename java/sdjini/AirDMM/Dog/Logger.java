package sdjini.AirDMM.Dog;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;

import sdjini.AirDMM.Setting;

public class Logger {
    private Activity Activity;
    private final Context Context;
    private final String ClassName;
    private final Log Log;
    private final File LogPath;
    public Logger(Activity Activity) {
        this.Activity = Activity;
        this.Context = this.Activity;
        this.ClassName = this.Activity.getLocalClassName();
        this.Log = new Log(this.Activity, this.ClassName);
        ZonedDateTime zdt = ZonedDateTime.now();
        this.LogPath = new File(this.Context.getFilesDir(),String.format(Locale.CHINA,"%s-%02d-%02d.csv",zdt.getYear(),zdt.getMonthValue(),zdt.getDayOfMonth()));
        try {
            if (!this.LogPath.exists()) this.LogPath.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Logger(Context Context, String ClassName){
        this.Context = Context;
        this.ClassName = ClassName;
        this.Log = new Log(this.Context, this.ClassName);
        ZonedDateTime zdt = ZonedDateTime.now();
        this.LogPath = new File(this.Context.getFilesDir(),String.format(Locale.CHINA,"%s-%02d-%02d.csv",zdt.getYear(),zdt.getMonthValue(),zdt.getDayOfMonth()));
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
            String logText = (String.join(
                    ",",
                    this.Log.Time,
                    this.Log.level.name(),
                    String.format(this.Log.Activity == null ? "Context: %s" : "Activity: %s", this.Log.Activity == null ? this.Log.Context : this.Log.Activity),
                    String.format("Class: %s", this.Log.Class),
                    this.Log.Tag,
                    this.Log.Message
            ) + "\n");
            try {
                if (!this.LogPath.exists()) this.LogPath.createNewFile();
                FileOutputStream fos = new FileOutputStream(LogPath, true);
                fos.write(logText.getBytes());
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch (this.Log.level){
                case step -> android.util.Log.v(this.Log.Tag,this.Log.Message);
                case debug -> android.util.Log.d(this.Log.Tag,this.Log.Message);
                case info -> android.util.Log.i(this.Log.Tag, this.Log.Message);
                case error -> android.util.Log.e(this.Log.Tag, this.Log.Message);
                case fatal -> android.util.Log.wtf(this.Log.Tag,this.Log.Message);
            }
    }
}
