package sdjini.AirDMM;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;
import sdjini.AirDMM.Neko.Config.Configure;
import sdjini.AirDMM.Neko.Config.Reader;
import sdjini.AirDMM.Neko.Config.Saver;
import sdjini.AirDMM.Neko.Permission.Checker;
import sdjini.AirDMM.Neko.Permission.Granter;

public class Setting extends AppCompatActivity {
    public static LogLevel outputLevel = LogLevel.step;
    public static Activity MainActivity;

    private Logger logger;
    private Saver notifyFloatWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.logger = new Logger(this, this.getClass().getName());
        logger.write(LogLevel.step, "Initialize", "Initialize Start");
        logger.write(LogLevel.step,"Initialize","Logger Initialized");

        notifyFloatWindow = new Saver(Configure.CFG_NOTIFY_WINDOW.FILE_NAME,this);
        MainActivity = this;


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        logger.write(LogLevel.step, "UI", "UI Initialized");

        Button Btn_Start = findViewById(R.id.Btn_Start);
        Button Btn_Stop = findViewById(R.id.Btn_Stop);
        Button Btn_Restart = findViewById(R.id.Btn_Restart);
        Button Btn_SaveConfig = findViewById(R.id.Btn_SaveConfig);

        logger.write(LogLevel.step, "Initialize", "Widget Initialized");

        Btn_Start.setOnClickListener(i -> Act_BtnStart());
        Btn_Stop.setOnClickListener(i -> Act_BtnStop());
        Btn_Restart.setOnClickListener(i -> Act_BtnRestart());
        Btn_SaveConfig.setOnClickListener(i -> Act_BtnSaveConfig());
        logger.write(LogLevel.step, "Initialize","BtnAct Initialized");

        Reader FloatViewConfig = new Reader(this, Configure.CFG_FLOATY_VIEW.FILE_NAME);
        Reader NotifyConfig = new Reader(this, Configure.CFG_NOTIFY.FILE_NAME);
        EditText Et_ActiveAlpha = findViewById(R.id.Et_ActiveAlpha);
        EditText Et_WaitingAlpha = findViewById(R.id.Et_WaitingAlpha);
        EditText Et_DelayTime = findViewById(R.id.Et_DelayTime);
        EditText Et_KeyWords = findViewById(R.id.Et_KeyWords);
        EditText Et_PodPackages = findViewById(R.id.Et_PodPackage);
        Et_ActiveAlpha.setText(FloatViewConfig.getString(Configure.CFG_FLOATY_VIEW.ACTIVITY_ALPHA));
        Et_WaitingAlpha.setText(FloatViewConfig.getString(Configure.CFG_FLOATY_VIEW.WAITING_ALPHA));
        Et_DelayTime.setText(String.valueOf(FloatViewConfig.getDouble(Configure.CFG_FLOATY_VIEW.DELAY_TIME)));
        Et_KeyWords.setText(String.valueOf(NotifyConfig.getString(Configure.CFG_NOTIFY.KEY_WORDS)));
        Et_PodPackages.setText(String.valueOf(NotifyConfig.getString(Configure.CFG_NOTIFY.POD_PACKAGES)));
        logger.write(LogLevel.step, "Initialize","EditText Initialized");

        logger.write(LogLevel.info, "Permission", "Check and Grant");
        if(!new Checker(this).SystemAlertWindow()){
            new Granter(this).SystemAlertWindow();
        }
        if (!new Checker(this).BindNotificationListenerService()){
            new Granter(this).BindNotificationListenerService();
        }

        TextView Tv_PodPackage = findViewById(R.id.Tv_PodPackage);
        Tv_PodPackage.setOnClickListener(v -> Toast.makeText(this,R.string.Pod,Toast.LENGTH_LONG).show());

        Reader notifyWindowConfig = new Reader(this,Configure.CFG_NOTIFY_WINDOW.FILE_NAME);
        Switch Swc_PodBlacklistMode = findViewById(R.id.Swc_PodBlacklistMode);
        Switch Swc_KeywordsBlacklistMode = findViewById(R.id.Swc_KeywordsBlacklistMode);

        Swc_PodBlacklistMode.setChecked(notifyWindowConfig.getBoolean(Configure.CFG_NOTIFY_WINDOW.POD_BLACKLIST_MODE));
        Swc_KeywordsBlacklistMode.setChecked(notifyWindowConfig.getBoolean(Configure.CFG_NOTIFY_WINDOW.KW_BLACKLIST_MODE));
        Swc_PodBlacklistMode.setOnCheckedChangeListener((c, b) -> notifyFloatWindow.saveBoolean(Configure.CFG_NOTIFY_WINDOW.POD_BLACKLIST_MODE, b));
        Swc_KeywordsBlacklistMode.setOnCheckedChangeListener((c, b) -> notifyFloatWindow.saveBoolean(Configure.CFG_NOTIFY_WINDOW.KW_BLACKLIST_MODE, b));
        logger.write(LogLevel.info, "Initialize", "Initialized");
    }

    private void Act_BtnStart(){
        logger.write(LogLevel.step,"BtnAct","Act_BtnStart");
        if(!Notify.floatWindowOn) {
            Notify.floatWindowOn = true;
            notifyFloatWindow.saveBoolean("floatWindowOn",Notify.floatWindowOn);
            try {
                Notify.setNotifyFloatWindow(Notify.floatWindowOn);
            }catch (NullPointerException e){
                Notify.floatWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
                Notify.floatView = LayoutInflater.from(this).inflate(R.layout.float_view, null);
                Notify.setNotifyFloatWindow(Notify.floatWindowOn);
            }
            logger.write(LogLevel.info, "BtnAct", "floatWindow Done");
        }
    }
    private void Act_BtnStop(){
        logger.write(LogLevel.step,"BtnAct","Act_BtnStop");
        if (Notify.floatWindowOn){
            Notify.floatWindowOn = false;
            notifyFloatWindow.saveBoolean("floatWindowOn",Notify.floatWindowOn);
            Notify.setNotifyFloatWindow(Notify.floatWindowOn);
            Notify.floatWindow.removeView(Notify.floatView);
            logger.write(LogLevel.info,"BtnAct","floatWindow closed");
        }
    }
    private void Act_BtnRestart(){
        logger.write(LogLevel.step,"BtnAct","Act_BtnRestart");
        Act_BtnStop();
        Act_BtnStart();
        logger.write(LogLevel.info, "BtnAct", "floatWindow restart");
    }
    private void Act_BtnSaveConfig(){
        logger.write(LogLevel.step, "BtnAct", "BtnSaveConfig Click");

        EditText Et_ActiveAlpha = findViewById(R.id.Et_ActiveAlpha);
        EditText Et_WaitingAlpha = findViewById(R.id.Et_WaitingAlpha);
        EditText Et_DelayTime = findViewById(R.id.Et_DelayTime);
        EditText Et_KeyWords = findViewById(R.id.Et_KeyWords);
        EditText Et_PodPackages = findViewById(R.id.Et_PodPackage);
        logger.write(LogLevel.info, "BtnAct","Get Widgets");

        Saver floatView = new Saver(Configure.CFG_FLOATY_VIEW.FILE_NAME, this);
        Saver notify = new Saver(Configure.CFG_NOTIFY.FILE_NAME, this);
        logger.write(LogLevel.step, "BtnAct", "new ConfigSaver");

        String ActiveAlpha = String.valueOf(Et_ActiveAlpha.getText());
        String WaitingAlpha = String.valueOf(Et_WaitingAlpha.getText());
        String KeyWords = String.valueOf(Et_KeyWords.getText());
        String PodPackages = String.valueOf(Et_PodPackages.getText());
        double DelayTime = Float.parseFloat(String.valueOf(Et_DelayTime.getText()));

        if (!ActiveAlpha.isEmpty()) floatView.saveString(Configure.CFG_FLOATY_VIEW.ACTIVITY_ALPHA, ActiveAlpha);
        if (!WaitingAlpha.isEmpty()) floatView.saveString(Configure.CFG_FLOATY_VIEW.WAITING_ALPHA, WaitingAlpha);
        if (DelayTime >= 0) floatView.saveDouble(Configure.CFG_FLOATY_VIEW.DELAY_TIME, DelayTime);
        if (!KeyWords.isEmpty()) notify.saveString(Configure.CFG_NOTIFY.KEY_WORDS, KeyWords);
        if (!PodPackages.isEmpty()) notify.saveString(Configure.CFG_NOTIFY.POD_PACKAGES, PodPackages);
        logger.write(LogLevel.info, "BtnAct", "Config Save Done");
    }
}