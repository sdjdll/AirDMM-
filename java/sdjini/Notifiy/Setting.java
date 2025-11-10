package sdjini.Notify;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sdjini.Notify.Dog.LogLevel;
import sdjini.Notify.Dog.Logger;
import sdjini.Notify.Neko.Config.Saver;
import sdjini.Notify.Neko.Permission.Checker;
import sdjini.Notify.Neko.Permission.Granter;

/// Setting 用户主界面。用于设置Notify悬浮窗
public class Setting extends AppCompatActivity {
    public static LogLevel outputLevel = LogLevel.step;
    public Activity MainActivity;

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Start
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

        if(!new Checker(this).SystemAlertWindow()){
            new Granter(this).SystemAlertWindow();
        }
        if (!new Checker(this).BindNotificationListenerService()){
            new Granter(this).BindNotificationListenerService();
        }
    }

    private void Act_BtnStart(){}
    private void Act_BtnStop(){}
    private void Act_BtnRestart(){}
    private void Act_BtnSaveConfig(){
        logger.write(LogLevel.info, "Action", "BtnSaveConfig Click");

        EditText Et_ActiveAlpha = findViewById(R.id.Et_ActiveAlpha);
        EditText Et_WaitingAlpha = findViewById(R.id.Et_WaitingAlpha);
        EditText Et_DelayTime = findViewById(R.id.Et_DelayTime);
        logger.write(LogLevel.info, "Action","Get Widgets");

        Saver configSaver = new Saver(this);
        logger.write(LogLevel.step, "Action", "new ConfigSaver");

        configSaver.saveString("FloatView", "ActiveAlpha", String.valueOf(Et_ActiveAlpha.getText()));
        configSaver.saveString("FloatView", "WaitingAlpha", String.valueOf(Et_WaitingAlpha.getText()));
        configSaver.saveString("FloatView", "DelayTime", String.valueOf(Et_DelayTime.getText()));
        logger.write(LogLevel.info, "Action", "Config Save Done");
    }
}


