package sdjini.AirDMM.intents;

public class Intent_ServiceSelfRestart extends LocalIntent {

    public static final class Key{
        public static final String ISWORK = "ISWORK";
    }
    public Intent_ServiceSelfRestart(boolean isWork) {
        super(LocalIntentsName.ServiceSelfRestart);
        super.putExtra(Key.ISWORK, isWork);
    }
}
