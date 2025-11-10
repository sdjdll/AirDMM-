package sdjini.Notifiy.Neko.Permission;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import sdjini.Notifiy.Dog.LogLevel;
import sdjini.Notifiy.Dog.Logger;

public class Granter {
    private final Context context;
    private final Activity activity;
    private final Logger logger;
    public Granter(Activity activity){
        this.activity = activity;
        this.context = this.activity;
        logger = new Logger(this.activity, this.getClass().getName());
    }
    public void SystemAlertWindow(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + this.activity.getPackageName()));
        this.context.startService(intent);
        logger.write(LogLevel.info,"Permission",String.format("%s Grant", UsedPermission.SYSTEM_ALERT_WINDOW));
    }
    public void BindNotificationListenerService(){
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        this.activity.startActivityForResult(intent, 0);
    }
}
