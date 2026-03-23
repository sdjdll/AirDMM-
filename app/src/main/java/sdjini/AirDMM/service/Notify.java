package sdjini.AirDMM.service;

import static sdjini.AirDMM.StaticMain.*;
import static sdjini.AirDMM.intents.LocalIntent.LocalIntentsName;
import static sdjini.AirDMM.intents.LocalIntent.serviceSelfRestart;

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

import sdjini.AirDMM.StaticMain;
import sdjini.AirDMM.intents.Intent_ServiceSelfRestart;
import sdjini.AirDMM.intents.LocalIntent;
import sdjini.AirDMM.log.Level;
import sdjini.AirDMM.log.Logger;
import sdjini.AirDMM.log.Tags;
import sdjini.AirDMM.shared.SharedManager;

public class Notify extends NotificationListenerService {
    private static String[] keywordFilter = new String[0];
    private static String[] packageFilter = new String[0];
    private SharedManager sm;
    private LocalBroadcastManager lb;
    private Logger logger;
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        lb = LocalBroadcastManager.getInstance(this);
        sm = new SharedManager(this, SharedManager.ShaderName.Notify);
        logger = new Logger(this);

        keywordFilter = getFilterArray(sm.readString(keywordFilterKey));
        packageFilter = getFilterArray(sm.readString(packageFilterKey));
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
        Title += " " + extras.getString(Notification.EXTRA_TITLE);
        Content += extras.getString(Notification.EXTRA_TEXT);
        synchronized (staticQueue.lock){
            staticQueue.add(Title, Content);
        }
    }
    private StatusBarNotification filter(StatusBarNotification sbn){
        return sbn;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private native String stringFormat(String raw);
}