package sdjini.AirDMM;

import java.util.logging.Handler;

import sdjini.AirDMM.custom.CoupleQueue;


public class StaticMain {
    public static final String keywordFilterKey = "KeyWordFilterKey";
    public static final String packageFilterKey = "PackageFilterKey";
    public static final String OnKeywordFilterKey = "OnKeywordFilter";
    public static final String OnPackageFilterKey = "OnPackageFilter";
    public static final String DelayTimeKey = "DelayTimeKey";
    public static final String ActiveColorKey = "ActiveColorKey";
    public static final String ActiveTextColorKey = "ActiveTextColorKey";
    public static final String WaitingColorKey = "WaitingColorKey";
    public static final String WaitingTextColorKey = "WaitingTextColorKey";
    public static final String IsFloatyOn = "IsFloatyOn";
    public static final String FloatyPosition = "FloatyPosition";
    public static final CoupleQueue staticQueue = new CoupleQueue(0);
    public static Handler FloatWindowHandler;
    public static int Flags = 0;
    public static final int serverStart = 1;
    public static final int serverRestart = 2;
    public static final int serverSelfRestart = 4;
    public static final int serverStop = 8;
    public static final int serverSelfStop = 16;
    public static final int notify = 32;
    public static final int update = 64;
}
