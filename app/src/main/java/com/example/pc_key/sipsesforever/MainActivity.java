package com.example.pc_key.sipsesforever;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//скрытие верхей сторки
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//скрытие верхей сторки
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
    }
    

    public void onMyButtonClick(View v) {
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        startActivity(intent);
    }

    public void onMyButtonClick2(View v) {
        Intent intent = new Intent(MainActivity.this, Main3Activity.class);
        startActivity(intent);
    }

    public void onMyButtonClick0(View v) {
        moveTaskToBack(true);
        //finish();
        //this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
