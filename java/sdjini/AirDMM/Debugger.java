package sdjini.AirDMM;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import sdjini.AirDMM.Dog.Logger;

public class Debugger extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger);
        Logger logger = new Logger(this);
    }
}