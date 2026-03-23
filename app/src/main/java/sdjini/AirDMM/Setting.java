package sdjini.AirDMM;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.AirDMM.intents.Intent_ServerRestart;
import sdjini.AirDMM.intents.Intent_ServiceControl;
import sdjini.AirDMM.service.FloatWindow;
import sdjini.AirDMM.service.Notify;

public class Setting extends AppCompatActivity {

    private LocalBroadcastManager lb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        lb = LocalBroadcastManager.getInstance(this);

        findViewById(R.id.Btn_Start).setOnClickListener(v -> {
            Intent i = new Intent_ServiceControl.Builder(this, FloatWindow.class).setWorking(true).built();
            startService(i);
            lb.sendBroadcast(i);
        });
        findViewById(R.id.Btn_Restart).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServerRestart(this, FloatWindow.class)));
        findViewById(R.id.Btn_Stop).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServiceControl.Builder(this, FloatWindow.class).setWorking(false).built()));

        findViewById(R.id.Btn_SaveConfig).setOnClickListener(v->{});

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StaticMain.staticQueue.destroy();
    }
}