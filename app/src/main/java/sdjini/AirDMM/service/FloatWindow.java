package sdjini.AirDMM.service;

import static sdjini.AirDMM.StaticMain.DelayTimeKey;
import static sdjini.AirDMM.StaticMain.WaitingColorKey;
import static sdjini.AirDMM.StaticMain.WaitingTextColorKey;
import static sdjini.AirDMM.StaticMain.staticQueue;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.AirDMM.R;
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
    private Handler updateFloaty;
    private SharedManager sm;
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty Intent", intent.getAction());
            switch (intent.getAction()){
                case LocalIntent.intents + LocalIntent.serverStart          -> serverStart();
                case LocalIntent.intents + LocalIntent.serverRestart        -> serverRestart();
                case LocalIntent.intents + LocalIntent.serviceSelfRestart   -> serverSelfRestart();
                case LocalIntent.intents + LocalIntent.serviceStop          -> serverStop();
                case LocalIntent.intents + LocalIntent.serviceSelfStop      -> serverSelfStop();
                case LocalIntent.intents + LocalIntent.notify               -> hasNotify();
                default -> Default();
            }
        }
        private void Default(){}
        private void serverStart(){
            if (States.ServiceState == States.state.WorkingAndFloaty) return;
            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty start");
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
            logger.printAndWrite(Level.STEP, new Tags.Service.ServiceAction(context), "State:"+States.ServiceState);
            if (States.ServiceState != States.state.WorkingAndFloaty) return;
            floaty.removeView(view);
            States.ServiceState = States.state.Working;
        }
        private void serverSelfStop(){
            if (States.ServiceState != States.state.None);
        }
        private void hasNotify(){
            updateFloaty.postDelayed(runnable, sm.readInt(DelayTimeKey, 500));
        }
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String[] temp;
                synchronized (staticQueue.lock){
                    temp = staticQueue.get();
                }
                if (temp == null) return;
                TextView tv = view.findViewById(R.id.Tv_Title);
                tv.setText(temp[0]);
                tv = view.findViewById(R.id.Tv_Context);
                tv.setText(temp[1]);
                updateFloaty.postDelayed(this, sm.readInt(DelayTimeKey, 500));
            }
        };
    };
    private LocalBroadcastManager lb;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        logger = new Logger(this);
        sm = new SharedManager(this, SharedManager.ShaderName.Floaty);
        floaty = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        updateFloaty = new Handler(getMainLooper());

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

        lb = LocalBroadcastManager.getInstance(this);
        lb.registerReceiver(br, iF);

        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Initialized");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(this), "Service Already Started");
        return super.onStartCommand(intent, flags, startId);
    }
}