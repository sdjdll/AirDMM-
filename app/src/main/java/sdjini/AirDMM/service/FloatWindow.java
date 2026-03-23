package sdjini.AirDMM.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.ServiceState;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.AirDMM.R;
import sdjini.AirDMM.intents.LocalIntent.LocalIntentsName;
import sdjini.AirDMM.intents.LocalIntent;
import sdjini.AirDMM.log.Level;
import sdjini.AirDMM.log.Logger;
import sdjini.AirDMM.log.Tags;

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
    private IntentFilter iF = new IntentFilter();
    private Context context;
    private Logger logger;
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
                default -> Default();
            }
        }
        private void Default(){}
        private void serverStart(){
            logger.printAndWrite(Level.INFO, new Tags.Service.ServiceAction(), "Floaty start");
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_DRAWN_APPLICATION;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            floaty.addView(LayoutInflater.from(context).inflate(R.layout.float_view, null), params);
        }
        private void serverRestart(){}
        private void serverSelfRestart(){}
        private void serverStop(){
            floaty.removeView(LayoutInflater.from(context).inflate(R.layout.float_view, null));
            States.ServiceState = States.state.None;
        }
        private void serverSelfStop(){}
    };
    private LocalBroadcastManager lb;

    @Override
    public void onCreate() {
        super.onCreate();
        floaty = (WindowManager) getSystemService(WINDOW_SERVICE);
        context = this;
        logger = new Logger(this);

        iF.addAction(LocalIntentsName.ServerStart.toString());
        iF.addAction(LocalIntentsName.ServerRestart.toString());
        iF.addAction(LocalIntentsName.ServiceSelfRestart.toString());
        iF.addAction(LocalIntentsName.ServiceStop.toString());
        iF.addAction(LocalIntentsName.ServiceSelfStop.toString());

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