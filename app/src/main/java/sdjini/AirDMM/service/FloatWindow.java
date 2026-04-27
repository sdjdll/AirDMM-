package sdjini.AirDMM.service;

import static sdjini.AirDMM.StaticMain.ActiveColorKey;
import static sdjini.AirDMM.StaticMain.ActiveTextColorKey;
import static sdjini.AirDMM.StaticMain.DelayTimeKey;
import static sdjini.AirDMM.StaticMain.WaitingColorKey;
import static sdjini.AirDMM.StaticMain.WaitingTextColorKey;
import static sdjini.AirDMM.StaticMain.staticQueue;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.AirDMM.R;
import sdjini.AirDMM.Setting;
import sdjini.AirDMM.intents.LocalIntent.LocalIntentsName;
import sdjini.AirDMM.intents.LocalIntent;
import sdjini.AirDMM.log.*;
import sdjini.AirDMM.shared.SharedManager;

public class FloatWindow extends Service {
    public static final class States{
        public enum state{
            None,
            Working,
            WorkingAndFloaty
        }
        public static state ServiceState = state.None;
    }

    private WindowManager floaty;
    private View view;
    private IntentFilter iF = new IntentFilter();
    private Context context;
//    private Logger logger;
    private Handler updateFloaty, MainHandler;
    private SharedManager sm;
    private final Object NotifyLock = new Object();
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty Intent", intent.getAction());
            assert intent.getAction()  != null;
            switch (intent.getAction()){
                case LocalIntent.intents + LocalIntent.serverStart          -> serverStart();
                case LocalIntent.intents + LocalIntent.serverRestart        -> serverRestart();
                case LocalIntent.intents + LocalIntent.serviceSelfRestart   -> serverSelfRestart();
                case LocalIntent.intents + LocalIntent.serviceStop          -> serverStop();
                case LocalIntent.intents + LocalIntent.serviceSelfStop      -> serverSelfStop();
                case LocalIntent.intents + LocalIntent.notify               -> hasNotify();
                case LocalIntent.intents + LocalIntent.update               -> update();
                default -> Default();
            }
        }
        private void Default(){}
        private void serverStart(){
            if (States.ServiceState == States.state.WorkingAndFloaty) return;
//            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty start");
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            floaty.addView(view, params);
            States.ServiceState = States.state.WorkingAndFloaty;
        }
        private void serverRestart(){
            serverStop();
            serverStart();
        }
        private void serverSelfRestart(){}
        private void serverStop(){
//            logger.printAndWrite(Level.STEP, new Tags.Service.ServiceAction(context), "State:"+States.ServiceState);
            if (States.ServiceState != States.state.WorkingAndFloaty) return;
            floaty.removeView(view);
            States.ServiceState = States.state.Working;
        }
        private void serverSelfStop(){
            if (States.ServiceState != States.state.None);
        }
        private void hasNotify(){
            synchronized (NotifyLock){
                NotifyLock.notify();
            }
        }

        private void update(){
            TextView tv = view.findViewById(R.id.Tv_Title);
            tv.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
            tv.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
            tv = view.findViewById(R.id.Tv_Context);
            tv.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
            tv.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
        }
    };
    private LocalBroadcastManager lb;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            logger.printAndWrite(Level.STEP, new Tags.Service.Floaty.FloatyLoop(updateFloaty), "updateFloaty Loop");
            String[] temp;
            synchronized (staticQueue.lock){
                temp = staticQueue.get();
            }
            TextView Tv_Title = view.findViewById(R.id.Tv_Title);
            TextView Tv_Context = view.findViewById(R.id.Tv_Context);

            if (temp == null) {
                Tv_Title.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
                Tv_Title.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
                Tv_Context.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
                Tv_Context.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
                synchronized (NotifyLock){
                    try {
//                        logger.printAndWrite(Level.STEP, new Tags.Service.Floaty.FloatyLoop(updateFloaty), "updateFloaty Loop Lock");
                        NotifyLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                updateFloaty.post(this);
                return;
            }
            MainHandler.post(() -> {
                Tv_Title.setBackgroundColor(Color.parseColor(sm.readString(ActiveColorKey, getString(R.string.actingBg))));
                Tv_Title.setTextColor(Color.parseColor(sm.readString(ActiveTextColorKey, getString(R.string.actingTx))));
                Tv_Context.setBackgroundColor(Color.parseColor(sm.readString(ActiveColorKey, getString(R.string.actingBg))));
                Tv_Context.setTextColor(Color.parseColor(sm.readString(ActiveTextColorKey, getString(R.string.actingTx))));
                Tv_Title.setText(temp[0]);
                Tv_Context.setText(temp[1]);
            });

            updateFloaty.postDelayed(this, sm.readInt(DelayTimeKey, 3000));
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
//        logger = new Logger(this);
        sm = new SharedManager(this, SharedManager.ShaderName.Floaty);
        floaty = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        HandlerThread ht_floaty = new HandlerThread("updateFloaty");
        ht_floaty.start();
        updateFloaty = new Handler(ht_floaty.getLooper());
        MainHandler = new Handler(Looper.getMainLooper());
        startForeground(1, createNotification());

        TextView tv = view.findViewById(R.id.Tv_Title);
        tv.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
        tv.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
        tv = view.findViewById(R.id.Tv_Context);
        tv.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
        tv.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));

        iF.addAction(LocalIntentsName.ServerStart.toString());
        iF.addAction(LocalIntentsName.ServerRestart.toString());
        iF.addAction(LocalIntentsName.ServiceSelfRestart.toString());
        iF.addAction(LocalIntentsName.ServiceStop.toString());
        iF.addAction(LocalIntentsName.ServiceSelfStop.toString());
        iF.addAction(LocalIntentsName.Notify.toString());
        iF.addAction(LocalIntentsName.Update.toString());

        lb = LocalBroadcastManager.getInstance(this);
        lb.registerReceiver(br, iF);

        updateFloaty.post(runnable);
//        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(this), "Initialized");
    }
    private Notification createNotification(){
        NotificationChannel channel = new NotificationChannel("Floaty foreground", "Floaty foreground", NotificationManager.IMPORTANCE_NONE);
        channel.setDescription(getString(R.string.FloatyNotification));
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Floaty foreground")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("AirDMM! is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setContentIntent(
                        PendingIntent.getActivities(
                                this,
                                0,
                                new Intent[]{new Intent(this, Setting.class)},
                                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
                );
        return builder.build();
    }
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(context), "Notify Bind");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(context), "Notify Unbind");
        }
    };
    private static class FloatyBinder extends Binder{ }
    private FloatyBinder thisBinder = new FloatyBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return thisBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(this), "Service Already Started");
        return super.onStartCommand(intent, flags, startId);
    }
}