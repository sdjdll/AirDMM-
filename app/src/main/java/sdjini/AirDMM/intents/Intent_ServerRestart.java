package sdjini.AirDMM.intents;

import android.app.Service;
import android.content.Context;

public class Intent_ServerRestart extends LocalIntent {
    public Intent_ServerRestart(Context source, Class target){
        super(source,target, LocalIntentsName.ServerRestart);
    }
}
