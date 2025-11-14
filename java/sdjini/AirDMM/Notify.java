package sdjini.AirDMM;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;
import sdjini.AirDMM.Neko.Config.Reader;
import sdjini.AirDMM.Neko.KeepAlive.NotifyGuard;

public class Notify extends NotificationListenerService {
    public static WindowManager floatWindow;
    @SuppressLint("StaticFieldLeak")
    public static View floatView;
    public static boolean floatWindowOn = false;
    private Logger logger;
    private Deque<StatusBarNotification> statusBarNotifications = new LinkedList<>();
    private volatile boolean hasNotifications = false;
    private HandlerThread sbnThread;
    private Handler sbnHandler;
    private final Object lock = new Object();
    private int bindTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(this, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Logger Initialized");

        floatWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        logger.write(LogLevel.info,"Initialize","floatWindow Initialized");

        Reader notifyFloatWindow = new Reader(this,"notifyFloatWindow");
        Notify.floatWindowOn = notifyFloatWindow.getBoolean("floatWindowOn");
        Notify.setNotifyFloatWindow(Notify.floatWindowOn);
        logger.write(LogLevel.step,"SbnService",String.format("ShowFloatWindow: %b",floatWindowOn));

        sbnThread = new HandlerThread("SbnServiceBackgroundThread");
        sbnThread.start();
        sbnHandler = new Handler(sbnThread.getLooper());
        logger.write(LogLevel.info, "SbnService", "Background thread started.");

        NotificationChannel notificationChannel = new NotificationChannel("Notify","Notify",NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
        startForeground(101, new NotificationCompat.Builder(this, "Notify")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notify is Alive!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build());
        startForegroundService(new Intent(this, NotifyGuard.class).putExtra("Class", this.getClass()));
        bindService(new Intent(this, NotifyGuard.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                NotifyGuard notifyGuardService;
                NotifyGuard.GuardBinder binder = (NotifyGuard.GuardBinder) service;
                notifyGuardService = binder.getService();

                if (notifyGuardService != null) {
                    logger.write(LogLevel.info, "SbnService", "NotifyGuard Connected and instance retrieved.");
                    bindTime = 0;
                } else {
                    logger.write(LogLevel.error, "SbnService", "Failed to get NotifyGuard instance.");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (bindTime >= 5) logger.write(LogLevel.fatal,"Guard","Notify Connect fali");
                bindTime++;
                logger.write(LogLevel.error, "Guard", String.format("Notify Unconnect.Rebind Time:%s", bindTime));
                try {
                    Thread.sleep(bindTime * 1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                startForegroundService(new Intent(Notify.this, NotifyGuard.class).putExtra("Class", this.getClass()));
            }
        },BIND_AUTO_CREATE);
    }

    public static void setNotifyFloatWindow(boolean show){
        if (show) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.CENTER_VERTICAL;
            floatWindow.addView(floatView, params);
            floatWindowOn = true;
        } else {
            if (floatWindowOn)
                floatWindow.removeView(floatView);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sbnThread != null) {
            sbnThread.quitSafely();
        }
        logger.write(LogLevel.info, "SbnService", "Background thread stopped.Rebinding.");
        requestRebind(new ComponentName(this.getPackageName(), this.getClass().getName()));
    }

    private int parseColor(String colorString, int defaultColor) {
        if (colorString == null || colorString.isEmpty()) {
            logger.write(LogLevel.error, "FloatService", "Color string is null or empty. Using default.");
            return defaultColor;
        }
        try {
            return Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            logger.write(LogLevel.error, "FloatService", "Invalid color format: '" + colorString + "'. Using default." + e);
            return defaultColor;
        }
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        logger.write(LogLevel.step, "SbnService", "SBN Posted");
        if (sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE) == null && sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT) == null)
            return;
        statusBarNotifications.offerLast(sbn);
        synchronized (lock) {
            if (!hasNotifications) {
                hasNotifications = true;
                logger.write(LogLevel.step, "SbnService", "First SBN, starting background task.");
                sbnHandler.post(this::runNotificationProcessingLoop);
            }
        }
    }


    private void postOnDmm(){
        logger.write(LogLevel.step,"PoD","postOnDMM");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            try {
                StatusBarNotification sbn = statusBarNotifications.pollFirst();
                if(sbn == null) {
                    statusBarNotifications = new LinkedList<>();
                    hasNotifications = false;
                    return;
                }
                logger.write(LogLevel.info, "PoD", String.format("\rTitle:\t\t%s\rContent:\t%s", sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE), sbn.getNotification().extras.getString(Notification.EXTRA_TEXT)));
                floatView.post(()->{
                    TextView Tv_Title = floatView.findViewById(R.id.Tv_Title);
                        TextView Tv_Context = floatView.findViewById(R.id.Tv_Context);
                try{
                    Tv_Title.setText(this.getPackageManager().getApplicationLabel(this.getPackageManager().getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA)));
                } catch (PackageManager.NameNotFoundException e) {
                    Tv_Title.setText(sbn.getPackageName());
                }
                    Tv_Context.setText(MessageFormat.format("{0} {1}", sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE) == null ? "" : sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE) + ":", sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT) == null?"":sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT)));
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
    private void runNotificationProcessingLoop() {
        logger.write(LogLevel.info, "SbnService", "Background task started.");

        Reader reader = new Reader(this, "FloatView");


        try {
            int delayTime = reader.getInt("DelayTime");
            int activeColor = parseColor(reader.getString("ActiveAlpha"), getResources().getColor(R.color.acting, getResources().newTheme()));
            floatView.post(() -> floatView.setBackgroundColor(activeColor));
            while (hasNotifications) {
                logger.write(LogLevel.info, "SbnService", "Cycle");
                postOnDmm();
                Thread.sleep(delayTime * 1000L);
            }
        } catch (InterruptedException e) {
            logger.write(LogLevel.error, "SbnService", "Processing loop interrupted.");
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.write(LogLevel.fatal,"SbnService","Unknow Error");
            throw e;
        } finally {
            hasNotifications = false;
            logger.write(LogLevel.info, "SbnService", "End Cycle");
            int waitingColor = parseColor(reader.getString("WaitingAlpha"), getResources().getColor(R.color.waiting, getResources().newTheme()));
            floatView.post(() -> floatView.setBackgroundColor(waitingColor));
            logger.write(LogLevel.step, "SbnService", "Background task finished.");
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        logger.write(LogLevel.info, "onBind", "System is binding. Returning super's Binder.");
        return super.onBind(intent);
    }
}