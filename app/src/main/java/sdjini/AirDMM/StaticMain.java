package sdjini.AirDMM;

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
    public static final CoupleQueue staticQueue = new CoupleQueue(0);
}
