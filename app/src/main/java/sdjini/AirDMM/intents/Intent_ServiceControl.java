package sdjini.AirDMM.intents;

import android.content.Context;

public class Intent_ServiceControl extends LocalIntent {
    public static final String working = "working";
    public static class Builder{
        public Context source;
        public Class target;
        public boolean isWorking;
        public Builder(Context source, Class target){
            this.source = source;
            this.target = target;
        }

        public Builder setWorking(boolean working) {
            isWorking = working;
            return this;
        }

        public Intent_ServiceControl built() {
            return new Intent_ServiceControl(this);
        }
    }
    public Intent_ServiceControl(Context source, Class target) {
        super(source, target, LocalIntentsName.ServerStart);
    }
    public Intent_ServiceControl(Builder builder){
        super(builder.source, builder.target);
        super.putExtra(working, builder.isWorking);
    }
}
