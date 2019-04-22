package com.example.pc_key.sipsesforever;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;

public class Main3Activity extends AppCompatActivity {

    private Spinner spinner_firmness, spinner_speed;
    private SwitchCompat switch_time, switch_tilt;
    private EditText editText_time;
    private String selected;
    private Boolean selected_sw;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//скрытие верхей сторки
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//скрытие верхей сторки
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main3);

        spinner_firmness =  findViewById(R.id.spinner1);
        spinner_firmness.setSelection(MainActivity.prefs1.getInt("firmness", 1) - 1);

        spinner_speed = findViewById(R.id.spinner2);
        spinner_speed.setSelection(MainActivity.prefs2.getInt("speed", 1) - 1);


        switch_time =  findViewById(R.id.switch1);
        switch_time.setChecked(MainActivity.prefs3.getBoolean("time_mode", false));


        switch_tilt =  findViewById(R.id.switch2);
        switch_tilt.setChecked(MainActivity.prefs4.getBoolean("tilt", false));


        editText_time = findViewById(R.id.editText);
        editText_time.setText(MainActivity.prefs5.getString("time", ""));
    }

    public void onMyButtonClick3(View v) {
       // Intent intent1 = new Intent(Main3Activity.this, Main2Activity.class);

        selected = spinner_firmness.getSelectedItem().toString();
        editor = MainActivity.prefs1.edit();
        editor.putInt("firmness", Integer.parseInt(selected));
        editor.commit();
        //intent1.putExtra("FIRMNESS", Integer.parseInt(selected));

        selected = spinner_speed.getSelectedItem().toString();
        editor = MainActivity.prefs2.edit();
        editor.putInt("speed", Integer.parseInt(selected));
        editor.commit();
        //intent1.putExtra("SPEED", Integer.parseInt(selected));

        selected_sw = switch_time.isChecked();
        editor = MainActivity.prefs3.edit();
        editor.putBoolean("time_mode", selected_sw);
        editor.commit();

        selected_sw = switch_tilt.isChecked();
        editor = MainActivity.prefs4.edit();
        editor.putBoolean("tilt", selected_sw);
        editor.commit();

        selected = editText_time.getText().toString();
        editor = MainActivity.prefs5.edit();
        editor.putString("time", selected);
        editor.commit();

        if (!((switch_time.isChecked()) && (selected == ""))) {
            Intent intent = new Intent(Main3Activity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
