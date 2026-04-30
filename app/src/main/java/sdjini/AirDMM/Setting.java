package sdjini.AirDMM;

import static sdjini.AirDMM.StaticMain.*;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Set;

import sdjini.AirDMM.intents.Intent_ServerRestart;
import sdjini.AirDMM.intents.Intent_ServiceControl;
import sdjini.AirDMM.intents.Intent_Update;
import sdjini.AirDMM.intents.LocalIntent;
import sdjini.AirDMM.service.FloatWindow;
import sdjini.AirDMM.service.Notify;
import sdjini.AirDMM.shared.SharedManager;

public class Setting extends AppCompatActivity {
    private LocalBroadcastManager lb;
    private SharedManager floaty,notify;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = LocalBroadcastManager.getInstance(this);
        floaty = new SharedManager(this, SharedManager.ShaderName.Floaty);
        notify = new SharedManager(this, SharedManager.ShaderName.Notify);

        permissions();

        setContentView(R.layout.setting);
        findViewById(R.id.Btn_Start).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServiceControl(LocalIntent.LocalIntentsName.ServerStart)));
        findViewById(R.id.Btn_Restart).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServerRestart(this, FloatWindow.class)));
        findViewById(R.id.Btn_Stop).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServiceControl(LocalIntent.LocalIntentsName.ServiceStop)));

        findViewById(R.id.Btn_SaveConfig).setOnClickListener(v->{
            EditText et = findViewById(R.id.Et_ActiveColor);
                floaty.write(ActiveColorKey, et.getText().toString());
            et = findViewById(R.id.Et_ActiveTextColor);
                floaty.write(ActiveTextColorKey, et.getText().toString());
            et = findViewById(R.id.Et_WaitingColor);
                floaty.write(WaitingColorKey, et.getText().toString());
            et = findViewById(R.id.Et_WaitingTextColor);
                floaty.write(WaitingTextColorKey, et.getText().toString());
            et = findViewById(R.id.Et_DelayPostion);
                floaty.write(FloatyPosition, Integer.parseInt(et.getText().toString()));
            et = findViewById(R.id.Et_KeyWords);
                notify.write(keywordFilterKey, et.getText().toString());
            et = findViewById(R.id.Et_PodPackage);
                notify.write(packageFilterKey, et.getText().toString());
            et = findViewById(R.id.Et_DelayTime);
            floaty.write(DelayTimeKey, Integer.parseInt(et.getText().toString()));

            lb.sendBroadcast(new Intent_Update());
        });

        EditText et = findViewById(R.id.Et_ActiveColor);
            et.setText(floaty.readString(ActiveColorKey, getString(R.string.actingBg)));
        et = findViewById(R.id.Et_ActiveTextColor);
            et.setText(floaty.readString(ActiveTextColorKey, getString(R.string.actingTx)));
        et = findViewById(R.id.Et_WaitingColor);
            et.setText(floaty.readString(WaitingColorKey, getString(R.string.waitingBg)));
        et = findViewById(R.id.Et_WaitingTextColor);
            et.setText(floaty.readString(WaitingTextColorKey, getString(R.string.waitingTx)));
        et = findViewById(R.id.Et_DelayTime);
            et.setText(""+floaty.readInt(DelayTimeKey, 500));
        et = findViewById(R.id.Et_DelayPostion);
            et.setText(""+floaty.readInt(FloatyPosition,50));
        et = findViewById(R.id.Et_KeyWords);
            et.setText(notify.readString(keywordFilterKey));
        et = findViewById(R.id.Et_PodPackage);
            et.setText(notify.readString(packageFilterKey));

        Switch sw = findViewById(R.id.Swc_KeywordsBlacklistMode);
        sw.setOnCheckedChangeListener((c,b)-> {
            notify.write(OnKeywordFilterKey, b);
            lb.sendBroadcast(new Intent_Update());
        });
        sw.setChecked(notify.readBoolean(OnKeywordFilterKey, false));

        sw = findViewById(R.id.Swc_PodBlacklistMode);
        sw.setOnCheckedChangeListener((c,b)-> {
            notify.write(OnPackageFilterKey, b);
            c.setText(b ? R.string.PodBM : R.string.PodWM);
            lb.sendBroadcast(new Intent_Update());
        });
        sw.setChecked(notify.readBoolean(OnPackageFilterKey, true));
        sw.setText(sw.isEnabled() ? R.string.PodBM : R.string.PodWM);
    }

    private void permissions(){
        String packageName = getPackageName();
        if (!Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + packageName));
            startActivityForResult(intent, 0);
        }
        if(NotificationManagerCompat.from(this).areNotificationsEnabled()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            else{
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
                startActivityForResult(intent, 0);
            }
        }

        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                intent.putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, new ComponentName(this, Notify.class));
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}