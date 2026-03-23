package sdjini.AirDMM.intents;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class LocalIntent extends Intent{
    public LocalIntent(Context source, Class target) {
        super(source, target);
    }

    public static final String serverStart = "ServerStart";
    public static final String serverRestart = "ServerRestart";
    public static final String serviceSelfRestart = "ServiceSelfRestart";
    public static final String serviceStop = "ServiceStop";
    public static final String serviceSelfStop = "ServiceSelfStop";
    public static final String intents = "sdjini.AirDMM.intents.";
    public enum LocalIntentsName {
        ServerStart(serverStart),
        ServerRestart(serverRestart),
        ServiceSelfRestart(serviceSelfRestart),
        ServiceStop(serviceStop),
        ServiceSelfStop(serviceSelfStop),
        ;
        private final String value;
        LocalIntentsName(String value) {
            this.value = intents+value;
        }
        @NonNull
        @Override
        public String toString() {
            return value;
        }
    }

    protected LocalIntent(LocalIntentsName intentName) {
        super();
        super.setAction(intentName.toString());
    }

    protected LocalIntent(Context source, Class target, LocalIntentsName intentName){
        super(source, target);
        super.setAction(intentName.toString());
    }
}
