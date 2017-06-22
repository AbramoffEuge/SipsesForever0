package com.example.pc_key.sipsesforever;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

public class Main3Activity extends AppCompatActivity {

    private Spinner spinner_firmness, spinner_speed;
    private SwitchCompat switch_time, switch_tilt;
    private EditText editText_time;
    private String selected;
    private Boolean selected_sw;
    public static SharedPreferences prefs1, prefs2, prefs3, prefs4, prefs5;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//скрытие верхей сторки
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//скрытие верхей сторки
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main3);

        prefs1 = getSharedPreferences("firmness", MODE_PRIVATE);
        spinner_firmness = (Spinner) findViewById(R.id.spinner1);
        spinner_firmness.setSelection(prefs1.getInt("firmness", 1) - 1);

        prefs2 = getSharedPreferences("speed", MODE_PRIVATE);
        spinner_speed = (Spinner) findViewById(R.id.spinner2);
        spinner_speed.setSelection(prefs2.getInt("speed", 1) - 1);

        prefs3 = getSharedPreferences("time_mode", MODE_PRIVATE);
        switch_time = (SwitchCompat) findViewById(R.id.switch1);
        switch_time.setChecked(prefs3.getBoolean("time_mode", false));

        prefs4 = getSharedPreferences("tilt", MODE_PRIVATE);
        switch_tilt = (SwitchCompat) findViewById(R.id.switch2);
        switch_tilt.setChecked(prefs4.getBoolean("tilt", false));

        prefs5 = getSharedPreferences("time", MODE_PRIVATE);
        editText_time = (EditText) findViewById(R.id.editText);
        editText_time.setText(prefs5.getString("time", null));
    }
    public void onMyButtonClick3(View v) {
        Intent intent1 = new Intent(Main3Activity.this, Main2Activity.class);

        selected = spinner_firmness.getSelectedItem().toString();
        editor = prefs1.edit();
        editor.putInt("firmness", Integer.parseInt(selected));
        editor.commit();
        //intent1.putExtra("FIRMNESS", Integer.parseInt(selected));

        selected = spinner_speed.getSelectedItem().toString();
        editor = prefs2.edit();
        editor.putInt("speed", Integer.parseInt(selected));
        editor.commit();
        //intent1.putExtra("SPEED", Integer.parseInt(selected));

        selected_sw = switch_time.isChecked();
        editor = prefs3.edit();
        editor.putBoolean("time_mode", selected_sw);
        editor.commit();

        selected_sw = switch_tilt.isChecked();
        editor = prefs4.edit();
        editor.putBoolean("tilt", selected_sw);
        editor.commit();

        selected = editText_time.getText().toString();
        editor = prefs5.edit();
        editor.putString("time", selected);
        editor.commit();

        if (!((switch_time.isChecked())&&(selected == ""))) {
            Intent intent = new Intent(Main3Activity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
