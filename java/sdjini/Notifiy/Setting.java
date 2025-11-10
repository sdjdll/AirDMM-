package sdjini.Notifiy;

import android.app.Activity;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sdjini.Notifiy.Dog.LogLevel;
import sdjini.Notifiy.Dog.Logger;
import sdjini.Notifiy.Neko.Permission.Checker;
import sdjini.Notifiy.Neko.Permission.Granter;

/// Setting 用户主界面。用于设置Notify悬浮窗
public class Setting extends AppCompatActivity {
    public static LogLevel outputLevel;
    public Activity MainActivity;

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Start
        super.onCreate(savedInstanceState);

        this.MainActivity = this;
        this.logger = new Logger(this, this.getClass().getName());

        logger.write(LogLevel.info,"Initialize","Logger Initialized");
        logger.write(LogLevel.info, "Initialize", "Initialize Start");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        logger.write(LogLevel.step, "UI", "UI Initialized");

        if(!new Checker(this).SystemAlertWindow()){
            new Granter(this).SystemAlertWindow();
        }
    }
}


