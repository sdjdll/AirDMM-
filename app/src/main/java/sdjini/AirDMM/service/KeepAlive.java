package sdjini.AirDMM.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepAlive extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}