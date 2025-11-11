package sdjini.AirDMM;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;
import sdjini.AirDMM.Neko.Config.Reader;
import sdjini.AirDMM.Neko.Config.Saver;

public class Notify extends NotificationListenerService {
    public static WindowManager floatWindow;
    @SuppressLint("StaticFieldLeak")
    public static View floatView;
    public static boolean floatWindowOn = false;
    private Logger logger;
    private final Deque<StatusBarNotification> statusBarNotifications = new LinkedList<>();
    private volatile boolean hasNotifications = false;
    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(this, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Logger Initialized");

        floatWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.float_view, null);
        logger.write(LogLevel.info,"Initialize","floatWindow Initialized");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        logger.write(LogLevel.step, "SbnService","SBN Post");

        statusBarNotifications.offerLast(sbn);
        logger.write(LogLevel.info, "SbnService","offerLast");

        if (!hasNotifications) {
            hasNotifications = true;
            logger.write(LogLevel.step, "SbnService","first Sbn");
            new Thread(() -> {
                logger.write(LogLevel.info, "SbnService","new SbnService Thread");
                floatView.post(() -> floatView.setBackgroundColor(Color.parseColor(new Reader(this, "FloatView").getString("ActiveAlpha"))));
                while (hasNotifications) {
                    logger.write(LogLevel.info, "SbnService", "Cycle");
                    try {
                        Thread.sleep((new Reader(this, "FloatView").getInt("DelayTime") * 100L));
                        postOnDmm();
                        Thread.sleep((new Reader(this, "FloatView").getInt("DelayTime") * 900L));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                hasNotifications = false;
                logger.write(LogLevel.info,"SbnService","End Cycle");
                floatView.post(() -> floatView.setBackgroundColor(Color.parseColor(new Reader(this, "FloatView").getString("WaitingAlpha"))));
                logger.write(LogLevel.step,"SbnService","End Thread");
            }).start();
        }
    }

    private void postOnDmm(){
        logger.write(LogLevel.step,"PoD","postOnDMM");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            try {
                StatusBarNotification sbn = statusBarNotifications.getFirst();
                logger.write(LogLevel.info, "PoD", String.format("\rTitle:\t\t%s\rContent:\t%s", sbn.getNotification().extras.getString(Notification.EXTRA_TITLE), sbn.getNotification().extras.getString(Notification.EXTRA_TEXT)));
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
                logger.write(LogLevel.info, "PoD", "removeFirst");
                hasNotifications = !statusBarNotifications.isEmpty();
                logger.write(LogLevel.step, "PoD",String.format("hasNotifications: %b",hasNotifications));
            }catch (NoSuchElementException e){
                logger.write(LogLevel.error, "PoD",String.format("First Element not Found: LinkedArray: %s\nError: %s", Arrays.toString(statusBarNotifications.toArray()), e));
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.write(LogLevel.info,"Service","onBind");
        return super.onBind(intent);
    }
}