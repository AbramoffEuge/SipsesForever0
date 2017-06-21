package com.example.pc_key.sipsesforever;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView1;
    private TextView textView2;
    public static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//скрытие верхей сторки
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//скрытие верхей сторки
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        textView1= (TextView)findViewById(R.id.score);
        textView2= (TextView)findViewById(R.id.record);
        prefs = getSharedPreferences("key", Context.MODE_PRIVATE);
        //textView1.setText("SCORE : " + prefs.getInt("score", 0));
        textView2.setText("BEST SCORE : " + prefs.getInt("key", 0));


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getSerializableExtra("SCORE") != null)
            textView1.setText("SCORE : " + getIntent().getSerializableExtra("SCORE"));
        else
            textView1.setText("SCORE : " + "-");
        textView2.setText("BEST SCORE : " + prefs.getInt("key", 0));
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
