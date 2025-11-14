package sdjini.AirDMM.Neko.Permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import sdjini.AirDMM.Dog.LogLevel;
import sdjini.AirDMM.Dog.Logger;

public class Checker {
    private final Activity activity;
    private final Context context;
    private final Logger logger;

    public Checker(Activity activity) {
        this.activity = activity;
        this.context = this.activity;
        this.logger = new Logger(this.activity, this.getClass().getName());
        logger.write(LogLevel.info,"Initialize","Checker Initialized");
    }

    public boolean SystemAlertWindow() {
        if (Settings.canDrawOverlays(this.context)) logger.write(LogLevel.info, "Permission", String.format("%s granted", UsedPermission.SYSTEM_ALERT_WINDOW));
        else logger.write(LogLevel.error, "Permission", String.format("%s not granted, please regrant permission", UsedPermission.SYSTEM_ALERT_WINDOW));
        return Settings.canDrawOverlays(this.context);
    }
    public boolean BindNotificationListenerService(){
        if (NotificationManagerCompat.getEnabledListenerPackages(this.context).contains(this.context.getPackageName())) logger.write(LogLevel.info, "Permission", String.format("%s granted", UsedPermission.BIND_NOTIFICATION_LISTENER_SERVICE));
        else logger.write(LogLevel.error, "Permission", String.format("%s not granted, please regrant permission", UsedPermission.BIND_NOTIFICATION_LISTENER_SERVICE));
        return NotificationManagerCompat.getEnabledListenerPackages(this.context).contains(this.context.getPackageName());
    }

    public boolean ForegroundService(){
        logger.write(LogLevel.info, "Permission", (ContextCompat.checkSelfPermission(this.context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) ? String.format("%s granted", UsedPermission.FOREGROUND_SERVICE) : String.format("%s not granted", UsedPermission.FOREGROUND_SERVICE));
        return ContextCompat.checkSelfPermission(this.context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED;
    }
}
