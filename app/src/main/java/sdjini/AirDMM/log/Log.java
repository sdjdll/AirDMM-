package sdjini.AirDMM.log;

import static sdjini.AirDMM.log.Level.STEP;
import static sdjini.AirDMM.log.Level.INFO;
import static sdjini.AirDMM.log.Level.ERROR;
import static sdjini.AirDMM.log.Level.FATAL;

import android.content.Context;
import android.icu.text.DateIntervalFormat;
import android.icu.text.SimpleDateFormat;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Log {
    private Level LogLevel;
    private Tag LogTag;
    private String LogContent, LogSupplement;
    private Exception LogException;
    private Context LogSource;
    protected Log(Context LogSource){
        this(null, null, null, null, null, null);
    }

    public Log(Level LogLevel,Context LogSource,Tag LogTag, String LogContent, String LogSupplement, Exception LogException) {
        this.LogLevel = LogLevel;
        this.LogSource = LogSource;
        this.LogContent = LogContent;
        this.LogSupplement = LogSupplement;
        this.LogException = LogException;
    }

    public void setLogContent(String logContent) {
        LogContent = logContent;
    }

    public void setLogException(Exception logException) {
        LogException = logException;
    }

    public void setLogLevel(Level logLevel) {
        LogLevel = logLevel;
    }

    public void setLogSource(Context logSource) {
        LogSource = logSource;
    }

    public void setLogSupplement(String logSupplement) {
        LogSupplement = logSupplement;
    }

    public void setLogTag(Tag logTag) {
        LogTag = logTag;
    }

    @NonNull
    @Override
    public String toString() {
        if (LogException == null){
            return String.format("%s,%s,%s,%s,%s,%s,null\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), LogSource.getClass(), LogLevel, LogTag, LogContent, LogSupplement);
        } else {
            StackTraceElement[] ste = LogException.getStackTrace();
            StringBuilder StackTrace = new StringBuilder(Objects.requireNonNull(LogException.getMessage()));
            for (StackTraceElement e : ste) StackTrace.append(":").append(e);
            return String.format("%s,%s,%s,%s,%s,%s,%s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), LogSource.getClass(), LogLevel, LogTag, LogContent, LogSupplement, StackTrace);
        }
    }

}
