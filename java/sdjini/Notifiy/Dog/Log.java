package sdjini.Notify.Dog;

import android.app.Activity;

import java.text.MessageFormat;
import java.util.Date;

public class Log {
    protected String Time;
    protected String Class;
    protected Activity Activity;
    protected LogLevel level;
    protected String Tag;
    protected String Message;

//    public String updateTime(){
//        Date date = new Date();
//        this.Time = MessageFormat.format("[{0}/{1}/{2} {3}:{4}:{5}]",date.getYear(),date.getMonth(),date.getDay(),date.getHours(),date.getMinutes(),date.getSeconds());
//        return this.Time;
//    }
    public void updateTime(){
        Date date = new Date();
        this.Time = MessageFormat.format("[{0}/{1}/{2} {3}:{4}:{5}]",date.getYear() - 100,date.getMonth()+1,date.getDate(),date.getHours(),date.getMinutes(),date.getSeconds());
    }

    protected Log(Activity Activity){
        this.Tag = "Unknow";
        this.level = LogLevel.step;
        this.Activity = Activity;
        this.Class = this.Activity.getLocalClassName();
    }
    protected Log(Activity Activity, LogLevel level){
        this.Tag = "Unknow";
        this.level = level;
        this.Activity = Activity;
        this.Class = this.Activity.getLocalClassName();
    }
    protected Log(Activity Activity, String Class){
        this.Tag = "Unknow";
        this.level = null;
        this.Activity = Activity;
        this.Class = Class;
    }
    protected Log(Activity Activity, String Class, LogLevel level){
        this.Tag = "Unknow";
        this.level = level;
        this.Activity = Activity;
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
