package sdjini.AirDMM.intents;

public class ServiceSelfRestartIntent extends LocalIntent {

    public static final class Key{
        public static final String ISWORK = "ISWORK";
    }
    protected ServiceSelfRestartIntent(boolean isWork) {
        super(LocalIntentsName.ServiceSelfRestart);
        super.putExtra(Key.ISWORK, isWork);
    }
}
