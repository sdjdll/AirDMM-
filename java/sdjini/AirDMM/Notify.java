package sdjini.AirDMM;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.List;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;

public class Notify extends NotificationListenerService {
    public static WindowManager floatWindow;
    @SuppressLint("StaticFieldLeak")
    public static View floatView;
    private Logger logger;
    public static List<StatusBarNotification> statusBarNotifications;
    private static boolean hasNotifications = false;
    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(this, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Logger Initialized");

        floatWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        logger.write(LogLevel.info,"Initialize","floatWindow Initialized");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) statusBarNotifications.addLast(sbn);
        if (!hasNotifications){
            hasNotifications = true;
            postOnDmm();
        }
    }

    private void postOnDmm(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            StatusBarNotification sbn = statusBarNotifications.getFirst();
            floatView.post(()->{
                TextView Tv_Title = floatView.findViewById(R.id.Tv_Title);
                TextView Tv_Context = floatView.findViewById(R.id.Tv_Context);
                try{
                    Tv_Title.setText(this.getPackageManager().getApplicationLabel(this.getPackageManager().getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA)));
                } catch (PackageManager.NameNotFoundException e) {
                    Tv_Title.setText(sbn.getPackageName());
                }
                Tv_Context.setText(MessageFormat.format("{0}:{1}" , sbn.getNotification().extras.getString(Notification.EXTRA_TITLE), sbn.getNotification().extras.getString(Notification.EXTRA_TEXT)));
            });
            statusBarNotifications.removeFirst();
            hasNotifications = !statusBarNotifications.isEmpty();
        }
    }
    private void post(StatusBarNotification sbn){
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.write(LogLevel.info,"Service","onBind");
        return super.onBind(intent);
    }
}