package sdjini.AirDMM.Neko.KeepAlive;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;
import sdjini.AirDMM.Notify;
import sdjini.AirDMM.R;

public class NotifyGuard extends Service {
    private Logger logger;
    private int bindTime = 0;
    private final IBinder GuardBinder = new GuardBinder();
    private final ServiceConnection scheduleNotifyRestart = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.write("Guard","Notify Connected");
            bindTime = 0;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (bindTime >= 5) logger.write(LogLevel.fatal,"Guard","Notify Connect failure");
            bindTime++;
            logger.write(LogLevel.error, "Guard", String.format("Notify Unconnect.Rebind Time:%s", bindTime));
            try {
                Thread.sleep(bindTime * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.write(LogLevel.step,"Guard","bindNotify");
            bindingNotify();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(this,this.getClass().getName());
        logger.write(LogLevel.info,"Guard","NotifyGuard onCreate");
        NotificationChannel notificationChannel = new NotificationChannel("NotifyGuard","NotifyGuard",NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
        logger.write(LogLevel.step,"Guard", "startForeground");
        startForeground(100, new NotificationCompat.Builder(this,"NotifyGuard")
                .setContentTitle("AirDMM! is Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build());
        bindingNotify();
    }
    private void bindingNotify(){
        logger.write(LogLevel.info,"Guard","Binding Notify");
        Intent intent = new Intent(this, Notify.class);
        bindService(intent, scheduleNotifyRestart, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        logger.write(LogLevel.error,"Guard","Guard Destroy.Check and try restart.");
        unbindService(scheduleNotifyRestart);
        super.onDestroy();
    }
    public class GuardBinder extends Binder{
        public NotifyGuard getService(){
            return NotifyGuard.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger = new Logger(this,this.getClass().getName());
        logger.write(LogLevel.info,"Guard","Guard bind");
        return new GuardBinder();
    }
}