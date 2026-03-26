package sdjini.AirDMM.service;

import static sdjini.AirDMM.StaticMain.*;
import static sdjini.AirDMM.intents.LocalIntent.LocalIntentsName;
import static sdjini.AirDMM.intents.LocalIntent.intents;
import static sdjini.AirDMM.intents.LocalIntent.update;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;

import sdjini.AirDMM.StaticMain;
import sdjini.AirDMM.custom.CoupleQueue;
import sdjini.AirDMM.intents.Intent_Notify;
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
    private LocalBroadcastManager lb;
    private Logger logger;
    private Context context;
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case intents+update -> Update();
                default -> Default();
            }
        }
        private void Default(){
            logger.printAndWrite(Level.ERROR, new Tags.Service.ServiceAction(context), "Default");
        }
        private void Update(){
            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(context), "Update");
            keywordFilter = getFilterArray(sm.readString(keywordFilterKey));
            packageFilter = getFilterArray(sm.readString(packageFilterKey));
            OnKeywordFilter = sm.readBoolean(OnKeywordFilterKey);
            OnPackageFilter = sm.readBoolean(OnPackageFilterKey);
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        staticQueue.INIT(10);
        lb = LocalBroadcastManager.getInstance(this);
        sm = new SharedManager(this, SharedManager.ShaderName.Notify);
        logger = new Logger(this);
        context = this;

        keywordFilter = getFilterArray(sm.readString(keywordFilterKey));
        packageFilter = getFilterArray(sm.readString(packageFilterKey));
        OnKeywordFilter = sm.readBoolean(OnKeywordFilterKey);
        OnPackageFilter = sm.readBoolean(OnPackageFilterKey);
        IntentFilter iF = new IntentFilter();
        iF.addAction(LocalIntentsName.ServiceSelfRestart.toString());
        lb.registerReceiver(br,iF);

        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(this), "Initialized");
    }
    private String[] getFilterArray(String raw){
        assert raw != null;
        return raw.split(",");
    }

    @Override
    public void onDestroy() {
        staticQueue.destroy();
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
        lb.sendBroadcast(new Intent_Notify());
    }
    private StatusBarNotification filter(StatusBarNotification sbn){
        return Arrays.asList(packageFilter).contains(sbn.getPackageName()) | !OnPackageFilter ? sbn : null;
    }
    private boolean filter(String s){
        boolean b;
        for (String key : keywordFilter) {
            b = s.contains(key);
            if (b) return true;
        }
        return OnKeywordFilter;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private native String stringFormat(String raw);
    private String stringFormatJ(String raw){
        if (raw == null) return null;

        StringBuilder sb = new StringBuilder(raw.length());

        for (int i = 0; i < raw.length(); ) {
            int codePoint = raw.codePointAt(i);
            if (!isInvisible(codePoint)) sb.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);
        }

        return sb.toString();
    }
    private boolean isInvisible(int codePoint) {
        if (codePoint == '\0' || codePoint == '\t' || codePoint == '\n') return false;

        if (Character.isSpaceChar(codePoint)) return true;
        return Character.isISOControl(codePoint);
    }
}