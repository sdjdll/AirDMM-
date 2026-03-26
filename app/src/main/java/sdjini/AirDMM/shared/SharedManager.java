package sdjini.AirDMM.shared;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class SharedManager {
    public enum ShaderName{
        Floaty("Floaty"),
        Notify("Notify"),
        ;
        private final String value;
        ShaderName(String s){ value = s; }

        @NonNull
        @Override
        public String toString() { return value; }
    }
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;
    public SharedManager(Context context, ShaderName name){
        this.sp = context.getSharedPreferences(name.toString(), Context.MODE_PRIVATE);
        this.editor = this.sp.edit();
    }

    public void write(String key, String s) {
        editor.putString(key, s);
        editor.apply();
    }

    public void write(String key, int i) {
        editor.putInt(key, i);
        editor.apply();
    }

    public void write(String key, long l) {
        editor.putLong(key, l);
        editor.apply();
    }

    public void write(String key, boolean b) {
        editor.putBoolean(key, b);
        editor.apply();
    }

    public void write(String key, float f) {
        editor.putFloat(key, f);
        editor.apply();
    }


    public String readString(String key, String... Default) {
        if (Default.length > 0) return sp.getString(key, Default[0]);
        return sp.getString(key,"");
    }
    public int readInt(String key, int... Default){
        if (Default.length > 0) return sp.getInt(key, Default[0]);
        return sp.getInt(key,-1);
    }
    public long readLong(String key, long... Default){
        if (Default.length > 0) return sp.getLong(key, Default[0]);
        return sp.getLong(key,-1);
    }
    public boolean readBoolean(String key, boolean... Default){
        if (Default.length > 0) return sp.getBoolean(key, Default[0]);
        return sp.getBoolean(key,false);
    }
    public float readFloat(String key, float... Default){
        if (Default.length > 0) return sp.getFloat(key, Default[0]);
        return sp.getFloat(key,-1f);
    }
}