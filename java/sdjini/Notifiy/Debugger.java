package sdjini.Notifiy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import sdjini.Notifiy.Dog.Log;
import sdjini.Notifiy.Dog.Logger;

public class Debugger extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger);
        Logger logger = new Logger(this);

    }
}