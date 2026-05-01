package sdjini.AirDMM.service;

import static sdjini.AirDMM.StaticMain.ActiveColorKey;
import static sdjini.AirDMM.StaticMain.ActiveTextColorKey;
import static sdjini.AirDMM.StaticMain.DelayTimeKey;
import static sdjini.AirDMM.StaticMain.FloatyPosition;
import static sdjini.AirDMM.StaticMain.WaitingColorKey;
import static sdjini.AirDMM.StaticMain.WaitingTextColorKey;
import static sdjini.AirDMM.StaticMain.IsFloatyOn;
import static sdjini.AirDMM.StaticMain.staticQueue;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.AirDMM.R;
import sdjini.AirDMM.Setting;
import sdjini.AirDMM.intents.Intent_ServiceControl;
import sdjini.AirDMM.intents.Intent_ServiceSelfRestart;
import sdjini.AirDMM.intents.LocalIntent.LocalIntentsName;
import sdjini.AirDMM.intents.LocalIntent;
import sdjini.AirDMM.log.Level;
import sdjini.AirDMM.log.Logger;
import sdjini.AirDMM.log.Tags;
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
    private Logger logger;
    private Handler updateFloaty, MainHandler;
    private SharedManager sm;
    private ViewGroup.MarginLayoutParams lp;
    private final Object NotifyLock = new Object();
    private int wc,
            wtc,
            ac,
            atc,
            dt,
            fp;
//            non = getColor(R.color.none);
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty Intent", intent.getAction());
            assert intent.getAction()  != null;
            switch (intent.getAction()){
                case LocalIntent.intents + LocalIntent.serverStart          -> serverStart();
                case LocalIntent.intents + LocalIntent.serverRestart        -> serverRestart();
                case LocalIntent.intents + LocalIntent.serviceSelfRestart   -> serverSelfRestart(intent);
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
            ac  = Color.parseColor(sm.readString(ActiveColorKey, getString(R.string.actingBg)));
            atc = Color.parseColor(sm.readString(ActiveTextColorKey, getString(R.string.actingTx)));
            wc  = Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg)));
            wtc = Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx)));
            dt = sm.readInt(DelayTimeKey, 500);
            fp = sm.readInt(FloatyPosition, 50);
            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty start");
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            params.format = PixelFormat.TRANSLUCENT;
            params.gravity = Gravity.TOP;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            floaty.addView(view, params);
            States.ServiceState = States.state.WorkingAndFloaty;
            sm.write(IsFloatyOn, true);
        }
        private void serverRestart(){
            serverStop();
            serverStart();
        }
        private void serverSelfRestart(Intent intent){
            if (intent.getBooleanExtra(Intent_ServiceSelfRestart.Key.ISWORK, false)) serverStart();
        }
        private void serverStop(){
            logger.printAndWrite(Level.STEP, new Tags.Service.ServiceAction(context), "State:"+States.ServiceState);
            if (States.ServiceState != States.state.WorkingAndFloaty) return;
            floaty.removeView(view);
            States.ServiceState = States.state.Working;
            sm.write(IsFloatyOn, false);
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
            LinearLayout Lout_Floaty = view.findViewById(R.id.Lout_Floaty);
            lp = (ViewGroup.MarginLayoutParams) Lout_Floaty.getLayoutParams();
            lp.topMargin = fp;
            Lout_Floaty.setLayoutParams(lp);
        }
    };
    private LocalBroadcastManager lb;
    private TextView Tv_Title;
    private TextView Tv_Context;
    private LinearLayout Lout;
    private final Runnable uiUpper = new Runnable() {
        private String[] couple;
        @Override
        public void run() {
            logger.printAndWrite(Level.STEP, new Tags.Service.Floaty.FloatyLoop(updateFloaty), "updateFloaty Loop");

            synchronized (staticQueue.lock){
                couple = staticQueue.get();
            }

            if (couple != null) toStart();
            else toStop();

            updateFloaty.postDelayed(this, dt);
        }
        private void toStop(){
            view.post(()->{
                // Tv清空可能造成抖动但是我没看到
//                Tv_Title.setText("");
//                Tv_Context.setText("");
                // 颜色
                Lout.setBackgroundColor(wc);
                Tv_Title.setTextColor(wtc);
                Tv_Context.setTextColor(wtc);
            });
            synchronized (NotifyLock){
                try {
                    NotifyLock.wait();
                } catch (InterruptedException e) {
                    logger.printAndWrite(Level.INFO, new Tags.Service.Floaty.FloatyLoop(updateFloaty), "Notify interrupted");
                }
            }
        }
        private void toStart(){
            view.post(()->{
                // 文字
                Tv_Title.setText(couple[0]);
                Tv_Context.setText(couple[1]);
                // 颜色
                Lout.setBackgroundColor(ac);
                Tv_Title.setTextColor(atc);
                Tv_Context.setTextColor(atc);
            });
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        lb = LocalBroadcastManager.getInstance(this);
        context = this;
        logger = new Logger(this);
        sm = new SharedManager(this, SharedManager.ShaderName.Floaty);
        ac  = Color.parseColor(sm.readString(ActiveColorKey, getString(R.string.actingBg)));
        atc = Color.parseColor(sm.readString(ActiveTextColorKey, getString(R.string.actingTx)));
        wc  = Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg)));
        wtc = Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx)));
        dt = sm.readInt(DelayTimeKey, 500);
        fp = sm.readInt(FloatyPosition, 50);
        floaty = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        HandlerThread ht_floaty = new HandlerThread("updateFloaty");
        ht_floaty.start();
        updateFloaty = new Handler(ht_floaty.getLooper());
        MainHandler = new Handler(Looper.getMainLooper());
        startForeground(1, createNotification());
        lb.sendBroadcast(new Intent_ServiceSelfRestart(sm.readBoolean(IsFloatyOn)));

        Tv_Title = view.findViewById(R.id.Tv_Title);
        Tv_Context = view.findViewById(R.id.Tv_Context);
        Lout = view.findViewById(R.id.Lout_Floaty);
        Tv_Title.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
        Tv_Title.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
        Tv_Context.setBackgroundColor(Color.parseColor(sm.readString(WaitingColorKey, getString(R.string.waitingBg))));
        Tv_Context.setTextColor(Color.parseColor(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx))));
        LinearLayout Lout_Floaty = view.findViewById(R.id.Lout_Floaty);
        lp = (ViewGroup.MarginLayoutParams) Lout_Floaty.getLayoutParams();
        lp.topMargin = fp;
        Lout_Floaty.setLayoutParams(lp);

        iF.addAction(LocalIntentsName.ServerStart.toString());
        iF.addAction(LocalIntentsName.ServerRestart.toString());
        iF.addAction(LocalIntentsName.ServiceSelfRestart.toString());
        iF.addAction(LocalIntentsName.ServiceStop.toString());
        iF.addAction(LocalIntentsName.ServiceSelfStop.toString());
        iF.addAction(LocalIntentsName.Notify.toString());
        iF.addAction(LocalIntentsName.Update.toString());

        lb.registerReceiver(br, iF);

        updateFloaty.post(uiUpper);
        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(this), "Initialized");
    }
    @NonNull
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("Floaty foreground", "Floaty foreground", NotificationManager.IMPORTANCE_NONE);
        channel.setDescription(getString(R.string.FloatyNotification));
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Floaty foreground")
                .setSmallIcon(R.drawable.icon)
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
    @Override
    public IBinder onBind(Intent intent) {
        return this.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.printAndWrite(Level.STEP, new Tags.Service.ServiceAction(this), "Service Already Started");
        lb.sendBroadcast(new Intent_ServiceSelfRestart(sm.readBoolean(IsFloatyOn)));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.printAndWrite(Level.ERROR, new Tags.Service.ServiceAction(this), "Error: Shouldn't Destroy");
        startForegroundService(new Intent_ServiceControl(this, this.getClass()));
    }
}