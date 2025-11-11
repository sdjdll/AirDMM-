package sdjini.AirDMM;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;
import sdjini.AirDMM.Neko.Config.Reader;
import sdjini.AirDMM.Neko.Config.Saver;
import sdjini.AirDMM.Neko.Permission.Checker;
import sdjini.AirDMM.Neko.Permission.Granter;

public class Setting extends AppCompatActivity {
    public static LogLevel outputLevel = LogLevel.step;
    public Activity MainActivity;

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.MainActivity = this;
        this.logger = new Logger(this, this.getClass().getName());

        logger.write(LogLevel.step,"Initialize","Logger Initialized");
        logger.write(LogLevel.step, "Initialize", "Initialize Start");

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

        Reader FloatViewConfig = new Reader(this, "FloatView");
        EditText Et_ActiveAlpha = findViewById(R.id.Et_ActiveAlpha);
        EditText Et_WaitingAlpha = findViewById(R.id.Et_WaitingAlpha);
        EditText Et_DelayTime = findViewById(R.id.Et_DelayTime);
        Et_ActiveAlpha.setText(FloatViewConfig.getString("ActiveAlpha"));
        Et_WaitingAlpha.setText(FloatViewConfig.getString("WaitingAlpha"));
        Et_DelayTime.setText(String.valueOf(FloatViewConfig.getInt("DelayTime")));
        logger.write(LogLevel.step, "Initialize","EditText Initialized");

        logger.write(LogLevel.info, "Permission", "Check and Grant");
        if(!new Checker(this).SystemAlertWindow()){
            new Granter(this).SystemAlertWindow();
        }
        if (!new Checker(this).BindNotificationListenerService()){
            new Granter(this).BindNotificationListenerService();
        }
        logger.write(LogLevel.info, "Initialize", "Initialized");
    }

    private void Act_BtnStart(){
        logger.write(LogLevel.step,"BtnAct","Act_BtnStart");
        if(!Notify.floatWindowOn) {
            Notify.floatWindowOn = true;
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.CENTER_VERTICAL;

            Notify.floatWindow.addView(Notify.floatView, params);
            logger.write(LogLevel.info, "BtnAct", "floatWindow Done");
        }
    }
    private void Act_BtnStop(){
        logger.write(LogLevel.step,"BtnAct","Act_BtnStop");
        if (Notify.floatWindowOn){
            Notify.floatWindowOn = false;
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
        logger.write(LogLevel.info, "BtnAct","Get Widgets");

        Saver configSaver = new Saver(this);
        logger.write(LogLevel.step, "BtnAct", "new ConfigSaver");

        String ActiveAlpha = String.valueOf(Et_ActiveAlpha.getText());
        String WaitingAlpha = String.valueOf(Et_WaitingAlpha.getText());
        int DelayTime = Integer.parseInt(String.valueOf(Et_DelayTime.getText()));
        if (!ActiveAlpha.isEmpty()) configSaver.saveString("FloatView", "ActiveAlpha", ActiveAlpha);
        if (!WaitingAlpha.isEmpty()) configSaver.saveString("FloatView", "WaitingAlpha", WaitingAlpha);
        if (DelayTime != 0) configSaver.saveInt("FloatView", "DelayTime", DelayTime);
        logger.write(LogLevel.info, "BtnAct", "Config Save Done");
    }
}


