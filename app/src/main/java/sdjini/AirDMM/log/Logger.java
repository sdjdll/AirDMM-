package sdjini.AirDMM.log;

import android.content.Context;
import android.os.Environment;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import sdjini.AirDMM.R;

public class Logger extends Log{
    private final Context Source;
    private final File LogFile;
    public Logger(Context Source) {
        super(Source);
        this.Source = Source;
        this.LogFile = new File(this.Source.getExternalFilesDir(null), Source.getString(R.string.app_name) + ".log");
    }

    public void printAndWrite(@UnsafeLevel @NotNull Level logLevel,@NotNull Tag LogTag, String Content, Exception e, String... Supplement) {
        setLogLevel(logLevel);
        setLogSource(Source);
        setLogTag(LogTag);
        setLogContent(Content);
        StringBuilder supplement = new StringBuilder(" ");
        if(Supplement != null)
            for (String s : Supplement) supplement.append(s).append(" ");
        setLogSupplement(supplement.toString());
        setLogException(e);
        try(FileOutputStream fos = new FileOutputStream(LogFile, true)){
            fos.write(super.toString().getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException ex) {
            staticStackPrinter(ex);
        }
        switch (logLevel){
            case FATAL -> android.util.Log.wtf(LogTag.toString(),"Content:"+Content + "Supplement:"+supplement,e);
            case ERROR -> android.util.Log.e(LogTag.toString(),"Content:"+Content + "Supplement:"+supplement,e);
            case INFO -> android.util.Log.i(LogTag.toString(),"Content:"+Content + "Supplement:"+supplement);
        }
    }

    public void printAndWrite(@SafeLevel Level logLevel, Tag LogTag, String Content,String... Supplement){
        setLogLevel(logLevel);
        setLogSource(Source);
        setLogTag(LogTag);
        setLogContent(Content);
        StringBuilder supplement = new StringBuilder(" ");
        if(Supplement != null)
            for (String s : Supplement) supplement.append(s).append(" ");
        setLogSupplement(supplement.toString());
        try(FileOutputStream fos = new FileOutputStream(LogFile, true)){
            fos.write(super.toString().getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            staticStackPrinter(e);
        }
        switch (logLevel){
            case INFO -> android.util.Log.i(LogTag.toString(),"Content:"+Content + " Supplement:"+supplement);
            case STEP -> android.util.Log.d(LogTag.toString(),"Content:"+Content + " Supplement:"+supplement);
        }
    }

    private static void staticStackPrinter(Exception e){
        System.err.println(e.getMessage());
        e.printStackTrace(System.err);
    }
}
