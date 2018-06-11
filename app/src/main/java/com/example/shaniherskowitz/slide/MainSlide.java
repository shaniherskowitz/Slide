package com.example.shaniherskowitz.slide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class MainSlide extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_slide);
        System.out.println("yayyy");

    }


    public void serviceSwitch(View view) {
        Switch s = (Switch)findViewById(R.id.switch1);
        if(s.isChecked()) {
            System.out.println("yayyy");
        } else {
            System.out.println("nayyy");
        }
    }
}
