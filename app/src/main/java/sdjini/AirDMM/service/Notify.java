package sdjini.AirDMM.service;

import static sdjini.AirDMM.StaticMain.*;
import static sdjini.AirDMM.service.FloatWindow.fws;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

import sdjini.AirDMM.log.Level;
import sdjini.AirDMM.log.Logger;
import sdjini.AirDMM.log.Tags;
import sdjini.AirDMM.shared.SharedManager;

public class Notify extends NotificationListenerService {
    private static String[] keywordFilter = new String[0];
    private static String[] packageFilter = new String[0];
    private boolean OnKeywordFilter = false;
    private boolean OnPackageFilter = false;
    private SharedManager sm;
    private Logger logger;
    private Context context;
    public static Notify notify;
    public void Update(){
        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(context), "Update");
        keywordFilter = getFilterArray(sm.readString(keywordFilterKey));
        packageFilter = getFilterArray(sm.readString(packageFilterKey));
        OnKeywordFilter = sm.readBoolean(OnKeywordFilterKey);
        OnPackageFilter = sm.readBoolean(OnPackageFilterKey);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        staticQueue.INIT(10);
        notify = this;
        sm = new SharedManager(this, SharedManager.ShaderName.Notify);
        logger = new Logger(this);
        context = this;

        keywordFilter = getFilterArray(sm.readString(keywordFilterKey));
        packageFilter = getFilterArray(sm.readString(packageFilterKey));
        OnKeywordFilter = sm.readBoolean(OnKeywordFilterKey);
        OnPackageFilter = sm.readBoolean(OnPackageFilterKey);

        startForegroundService(new Intent(this, FloatWindow.class));

        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(this), "Initialized");
    }
    @NonNull
    @Contract("null -> new")
    private String[] getFilterArray(String raw){
        if (raw == null) return new String[0];
        return raw.split(",");
    }

    @Override
    public void onDestroy() {
        staticQueue.destroy();
        notify = null;
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        PackageManager pm = getPackageManager();

        sbn = filter(sbn);
        if (sbn == null) return;
        Bundle extras = sbn.getNotification().extras;
        String Title = "", Content = "";
        try {
            Title += pm.getApplicationLabel(pm.getApplicationInfo(sbn.getPackageName(), 1));
        } catch (PackageManager.NameNotFoundException e) {
            Title += extras;
        }
        Title += Title.isEmpty() ? "" : ":" + extras.getString(Notification.EXTRA_TITLE);
        Content += extras.getString(Notification.EXTRA_TEXT);
        if (!filter(Title+Content)) return;

        Title = stringFormatJ(Title);
        Content = Content.length() < 1000 ? stringFormatJ(Content) : stringFormat(Content);

        synchronized (staticQueue.lock){
            staticQueue.add(Title,Content);
        }
        try{
            fws.hasNotify();
        }catch (NullPointerException e){
            logger.printAndWrite(Level.ERROR, new Tags.Service.ServiceAction(this), "FloatWindow is Stopped");
        } finally {
            startForegroundService(new Intent(this, FloatWindow.class));
        }
    }
    @Nullable
    private StatusBarNotification filter(StatusBarNotification sbn){
        if(OnPackageFilter) return Arrays.asList(packageFilter).contains(sbn.getPackageName()) ? null : sbn;
        else return Arrays.asList(packageFilter).contains(sbn.getPackageName()) ? sbn : null;
    }
    private boolean filter(String s){
        if(OnKeywordFilter)
            for (String key : keywordFilter)
                if (s.contains(key)) return false;
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private native String stringFormat(String raw);
    private String stringFormatJ(String raw) {
        int rawL = raw!=null? raw.length() : 0;
        if (rawL == 0) return null;
        StringBuilder sb = new StringBuilder(rawL);
        boolean lastWasInvisible = false;
        for (int i = 0; i < rawL; ) {
            int codePoint = raw.codePointAt(i);
            if (isInvisible(codePoint)) lastWasInvisible = true;
            else {
                if (lastWasInvisible) {
                    sb.append(' ');
                    lastWasInvisible = false;
                }
                sb.appendCodePoint(codePoint);
            }

            i += Character.charCount(codePoint);
        }
        return sb.toString().trim();
    }

    private boolean isInvisible(int codePoint) {
        if (codePoint == '\0' || codePoint == '\t' || codePoint == '\n') return false;

        if (Character.isSpaceChar(codePoint)) return true;
        return Character.isISOControl(codePoint);
    }
}