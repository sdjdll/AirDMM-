package sdjini.AirDMM.Dog;

import android.app.Activity;
import android.content.Context;

import java.time.ZonedDateTime;
import java.util.Locale;

public class Log {
    protected String Time;
    protected String Class;
    protected Activity Activity;
    protected Context Context;
    protected LogLevel level;
    protected String Tag;
    protected String Message;

public void updateTime(){
        ZonedDateTime date = ZonedDateTime.now();
        this.Time = String.format(Locale.CHINA,"[%s/%02d/%02d %02d:%02d:%02d]",date.getYear() ,date.getMonthValue(),date.getDayOfMonth(),date.getHour(),date.getMinute(),date.getSecond());
    }

    protected Log(Activity Activity){
        this.Tag = "Unknow";
        this.level = LogLevel.step;
        this.Activity = Activity;
        this.Class = this.Activity.getLocalClassName();
    }
    protected Log(Activity Activity, String Class){
        this.Tag = "Unknow";
        this.level = LogLevel.step;
        this.Activity = Activity;
        this.Class = Class;
    }
    protected Log(Activity Activity, LogLevel level){
        this.Tag = "Unknow";
        this.level = level;
        this.Activity = Activity;
        this.Class = this.Activity.getLocalClassName();
    }
    protected Log(Activity Activity, String Class, LogLevel level){
        this.Tag = "Unknow";
        this.level = level;
        this.Activity = Activity;
        this.Class = Class;
    }
    protected Log(Context Context, String Class){
        this.Tag = "Unknow";
        this.level = LogLevel.step;
        this.Context = Context;
        this.Class = Class;
    }

    protected void setTag(String Tag){
        this.Tag = Tag;
    }
    protected void setMessage(String Message){
        this.Message = Message;
    }
    protected void setLog(String Tag, String Message){
        this.Tag = Tag;
        this.Message = Message;
    }
    protected Log getLog(){
        return this;
    }
    protected void setLevel(LogLevel level){
        this.level = level;
    }
}
