package sdjini.AirDMM;

import static sdjini.AirDMM.StaticMain.*;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.AirDMM.intents.Intent_ServerRestart;
import sdjini.AirDMM.intents.Intent_ServiceControl;
import sdjini.AirDMM.intents.LocalIntent;
import sdjini.AirDMM.service.FloatWindow;
import sdjini.AirDMM.shared.SharedManager;

public class Setting extends AppCompatActivity {
    private LocalBroadcastManager lb;
    private SharedManager sm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = LocalBroadcastManager.getInstance(this);
        sm = new SharedManager(this, SharedManager.ShaderName.Floaty);

        setContentView(R.layout.activity_setting);
        findViewById(R.id.Btn_Start).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServiceControl(LocalIntent.LocalIntentsName.ServerStart)));
        findViewById(R.id.Btn_Restart).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServerRestart(this, FloatWindow.class)));
        findViewById(R.id.Btn_Stop).setOnClickListener(v -> lb.sendBroadcast(new Intent_ServiceControl(LocalIntent.LocalIntentsName.ServiceStop)));

        findViewById(R.id.Btn_SaveConfig).setOnClickListener(v->{
            EditText et = findViewById(R.id.Et_ActiveColor);
                sm.write(ActiveColorKey, et.getText().toString());
            et = findViewById(R.id.Et_ActivityTextColor);
                sm.write(ActiveTextColorKey, et.getText().toString());
            et = findViewById(R.id.Et_WaitingColor);
                sm.write(WaitingColorKey, et.getText().toString());
            et = findViewById(R.id.Et_WaitingTextColor);
                sm.write(WaitingTextColorKey, et.getText().toString());

            et = findViewById(R.id.Et_KeyWords);
                sm.write(keywordFilterKey, et.getText().toString());
            et = findViewById(R.id.Et_PodPackage);
                sm.write(packageFilterKey, et.getText().toString());
            et = findViewById(R.id.Et_DelayTime);
                sm.write(DelayTimeKey, Integer.parseInt(et.getText().toString()));
        });

        EditText et = findViewById(R.id.Et_ActiveColor);
            et.setText(sm.readString(ActiveColorKey, getString(R.string.actingBg)));
        et = findViewById(R.id.Et_ActivityTextColor);
            et.setText(sm.readString(ActiveTextColorKey, getString(R.string.actingTx)));
        et = findViewById(R.id.Et_WaitingColor);
            et.setText(sm.readString(WaitingColorKey, getString(R.string.waitingBg)));
        et = findViewById(R.id.Et_WaitingTextColor);
            et.setText(sm.readString(WaitingTextColorKey, getString(R.string.waitingTx)));

        et = findViewById(R.id.Et_KeyWords);
            et.setText(sm.readString(keywordFilterKey));
        et = findViewById(R.id.Et_PodPackage);
            et.setText(sm.readString(packageFilterKey));
        et = findViewById(R.id.Et_DelayTime);
            et.setText(""+sm.readInt(DelayTimeKey, 500));

        startService(new Intent_ServiceControl.Builder(this, FloatWindow.class).setWorking(true).built());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}