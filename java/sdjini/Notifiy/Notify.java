package sdjini.Notifiy;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

import sdjini.Notifiy.Dog.LogLevel;
import sdjini.Notifiy.Dog.Logger;

public class Notify extends NotificationListenerService {
    public static WindowManager floatWindow;
    @SuppressLint("StaticFieldLeak")
    public static View floatView;
    private Logger logger;
    public static List<StatusBarNotification> statusBarNotifications;
    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(null, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Logger Initialized");


        floatWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        logger.write(LogLevel.info,"Initialize","floatWindow Initialized");

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) statusBarNotifications.addLast(sbn);
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.write(LogLevel.info,"Service","onBind");
        return super.onBind(intent);
    }
}