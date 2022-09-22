package com.example.camtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.ToggleButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.opencv.core.Scalar;

public class SettingsActivity extends AppCompatActivity {

    //static int value = 0;
    Button buttonMode = null;
    Button button2 = null;
    Button button3 = null;
    Button button4 = null;
    LinearLayout linearLayout2 = null;
    LinearLayout linearLayout3 = null;
    LinearLayout linearLayout4 = null;
    ScrollView scrollView1 = null;
    Switch switch1 = null;
    TextInputEditText TextInputEditTextInputEq_bright_level = null;
    TextInputEditText TextInputEditTextInputRadius_for_bright_points_search = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //hideSystemUI();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //showSystemUI();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        buttonMode = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        linearLayout2 = findViewById(R.id.linearLayout2);
        linearLayout3 = findViewById(R.id.linearLayout3);
        linearLayout4 = findViewById(R.id.linearLayout4);
        switch1 = findViewById(R.id.switch1);
        scrollView1 = findViewById(R.id.scrollView1);
        TextInputEditTextInputEq_bright_level = findViewById(R.id.InputEq_bright_level);
        TextInputEditTextInputRadius_for_bright_points_search = findViewById(R.id.InputRadius_for_bright_points_search);
        mode_changed();
        /*linearLayout2.setVisibility(View.INVISIBLE);
        linearLayout4.setVisibility(View.INVISIBLE);
        buttonMode.setText("Режим созвездия");*/
        /*((EditText)findViewById(R.id.InputHueLow)).setText(String.valueOf(MainActivity.Hue_low));
        ((EditText)findViewById(R.id.InputSaturationLow)).setText(String.valueOf(MainActivity.Sat_low));
        ((EditText)findViewById(R.id.InputValueLow)).setText(String.valueOf(MainActivity.Val_low));
        ((EditText)findViewById(R.id.InputHueHigh)).setText(String.valueOf(MainActivity.Hue_high));
        ((EditText)findViewById(R.id.InputSaturationHigh)).setText(String.valueOf(MainActivity.Sat_high));
        ((EditText)findViewById(R.id.InputValueHigh)).setText(String.valueOf(MainActivity.Val_high));
        ((EditText)findViewById(R.id.InputALPHA)).setText(String.valueOf(MainActivity.alpha));
        ((EditText)findViewById(R.id.InputBETA)).setText(String.valueOf(MainActivity.beta));
        ((EditText)findViewById(R.id.InputValueLow)).setText(String.valueOf(MainActivity.th_MinVal));
        ((EditText)findViewById(R.id.InputValueHigh)).setText(String.valueOf(MainActivity.th_MaxVal));
        ((EditText)findViewById(R.id.InputSubmatRadius)).setText(String.valueOf(MainActivity.submat_radius));*/
        //TextView textViewInputHueLow = findViewById(R.id.InputHueLow);
        //textViewInputHueLow.setText(String.valueOf(MainActivity.Hue_low));

        buttonMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Mode = MainActivity.Mode + 1;
                if (MainActivity.Mode == 4) {MainActivity.Mode = 0;}
                mode_changed();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        //EditText InputHueLow = (EditText)findViewById(R.id.InputHueLow);


        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    MainActivity.automatic_hsv_levels = true;
                    linearLayout2.setPadding(0,int_to_dp(1000),0,0);
                } else {
                    // The toggle is disabled
                    MainActivity.automatic_hsv_levels = false;
                    linearLayout2.setPadding(int_to_dp(15),int_to_dp(35),int_to_dp(15),0);
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
         super.onBackPressed();
        if (MainActivity.Mode == 0) { //Созвездие монитор
            check_inputs_mode_012();
        }
        if (MainActivity.Mode == 1) { //Созвездие матрица
            check_inputs_mode_012();
        }
        if (MainActivity.Mode == 2) { //Лазер
            check_inputs_mode_012();
        }
        if (MainActivity.Mode == 3) { //Бегущая строка

        }
        save_settings();
    }

    void mode_changed(){
        if (MainActivity.Mode == 0) { //Созвездие монитор
            TextInputEditTextInputEq_bright_level.setEnabled(true);
            TextInputEditTextInputRadius_for_bright_points_search.setEnabled(true);
            switch1.setVisibility(View.VISIBLE);
            linearLayout2.setVisibility(View.VISIBLE);
            linearLayout4.setVisibility(View.INVISIBLE);
            linearLayout2.setPadding(int_to_dp(15),int_to_dp(35),int_to_dp(15),0);
            linearLayout4.setPadding(0,int_to_dp(1000),0,0);
            variable_to_textbox_012();
            buttonMode.setText("Режим созвездия - монитор");
            if (MainActivity.automatic_hsv_levels == true){switch1.setChecked(true); linearLayout2.setPadding(0,int_to_dp(1000),0,0);}
            else{switch1.setChecked(false); linearLayout2.setPadding(int_to_dp(15),int_to_dp(35),int_to_dp(15),0);}
        }
        if (MainActivity.Mode == 1) { //Созвездие матрица
            TextInputEditTextInputEq_bright_level.setEnabled(true);
            TextInputEditTextInputRadius_for_bright_points_search.setEnabled(true);
            switch1.setVisibility(View.VISIBLE);
            linearLayout2.setVisibility(View.VISIBLE);
            linearLayout4.setVisibility(View.INVISIBLE);
            linearLayout2.setPadding(int_to_dp(15),int_to_dp(35),int_to_dp(15),0);
            linearLayout4.setPadding(0,int_to_dp(1000),0,0);
            variable_to_textbox_012();
            buttonMode.setText("Режим созвездия - матрица");
            if (MainActivity.automatic_hsv_levels == true){switch1.setChecked(true); linearLayout2.setPadding(0,int_to_dp(1000),0,0);}
            else{switch1.setChecked(false); linearLayout2.setPadding(int_to_dp(15),int_to_dp(35),int_to_dp(15),0);}
        }
        if (MainActivity.Mode == 2) { //Лазер
            TextInputEditTextInputEq_bright_level.setEnabled(false);
            TextInputEditTextInputRadius_for_bright_points_search.setEnabled(false);
            switch1.setVisibility(View.INVISIBLE);
            linearLayout2.setVisibility(View.VISIBLE);
            linearLayout4.setVisibility(View.INVISIBLE);
            linearLayout2.setPadding(int_to_dp(15),int_to_dp(10),int_to_dp(15),0);
            linearLayout4.setPadding(0,int_to_dp(1000),0,0);
            variable_to_textbox_012();
            buttonMode.setText("Режим лазера");
        }
        if (MainActivity.Mode == 3) { //Бегущая строка
            linearLayout2.setVisibility(View.INVISIBLE);
            linearLayout4.setVisibility(View.VISIBLE);
            linearLayout2.setPadding(0,int_to_dp(1000),0,0);
            linearLayout4.setPadding(0,int_to_dp(10),0,0);
            buttonMode.setText("Режим бегущая строка");
        }
    }

    void save_settings(){
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("automatic_hsv_levels", MainActivity.automatic_hsv_levels);
        editor.putInt("Mode", MainActivity.Mode);
        editor.putInt("Hue_low", MainActivity.Hue_low);
        editor.putInt("Sat_low", MainActivity.Sat_low);
        editor.putInt("Val_low", MainActivity.Val_low);
        editor.putInt("Hue_high", MainActivity.Hue_high);
        editor.putInt("Sat_high", MainActivity.Sat_high);
        editor.putInt("Val_high", MainActivity.Val_high);
        editor.putInt("alpha", MainActivity.alpha);
        editor.putInt("beta", MainActivity.beta);
        editor.putInt("th_MinVal", MainActivity.th_MinVal);
        editor.putInt("th_MaxVal", MainActivity.th_MaxVal);
        editor.putInt("submat_radius", MainActivity.submat_radius);
        editor.putInt("radius_for_bright_points_search", MainActivity.radius_for_bright_points_search);
        editor.putInt("eq_bright_level", MainActivity.eq_bright_level);
        editor.apply();
    }

    void check_inputs_mode_012(){
        //value = Integer.parseInt(((EditText)findViewById(R.id.InputHueLow)).getText().toString());

        /* 1) HSV (цветовой фильтр, настройки маски):
        0..179 Hue (Оттенок), 0..255 Saturation (Насыщенность), 0.255 Value (Яркость)*/
        MainActivity.Hue_low = Integer.parseInt(((EditText)findViewById(R.id.InputHueLow)).getText().toString());
        MainActivity.Sat_low = Integer.parseInt(((EditText)findViewById(R.id.InputSaturationLow)).getText().toString());
        MainActivity.Val_low = Integer.parseInt(((EditText)findViewById(R.id.InputValueLow)).getText().toString());
        MainActivity.Hue_high = Integer.parseInt(((EditText)findViewById(R.id.InputHueHigh)).getText().toString());
        MainActivity.Sat_high = Integer.parseInt(((EditText)findViewById(R.id.InputSaturationHigh)).getText().toString());
        MainActivity.Val_high = Integer.parseInt(((EditText)findViewById(R.id.InputValueHigh)).getText().toString());
        MainActivity.s1 = new Scalar(MainActivity.Hue_low, MainActivity.Sat_low, MainActivity.Val_low);
        MainActivity.s2 = new Scalar(MainActivity.Hue_high, MainActivity.Sat_high, MainActivity.Val_high);

        /* 2) Яркость и контраст:
        ALPHA (яркость) - 1..3
        BETA (контраст) 0..100 */
        MainActivity.alpha = Integer.parseInt(((EditText)findViewById(R.id.InputALPHA)).getText().toString()); // Contrast control (1.0-3.0)
        MainActivity.beta = Integer.parseInt(((EditText)findViewById(R.id.InputBETA)).getText().toString());

        /* 3) Binary threshold (бинарный фильтр):
        MinVal: 0..255
        MaxVal: 0..255 */
        MainActivity.th_MinVal = Integer.parseInt(((EditText)findViewById(R.id.InputA)).getText().toString());
        MainActivity.th_MaxVal = Integer.parseInt(((EditText)findViewById(R.id.InputB)).getText().toString());

        /* 4) Submat (настройки подрезка картинки)
        submat_radius: 10..500
        */
        MainActivity.submat_radius = Integer.parseInt(((EditText)findViewById(R.id.InputSubmatRadius)).getText().toString());

        /* 5) Радиус точки закрашивающей яркие области
        submat_radius: 1..20 */
        MainActivity.radius_for_bright_points_search = Integer.parseInt(((EditText)findViewById(R.id.InputRadius_for_bright_points_search)).getText().toString());

        /* 6) Уровень допустимой однородности ярких точек, %
        submat_radius: 0..100 */
        MainActivity.eq_bright_level = Integer.parseInt(((EditText)findViewById(R.id.InputEq_bright_level)).getText().toString());

    }

    void variable_to_textbox_012(){
        ((EditText)findViewById(R.id.InputHueLow)).setText(String.valueOf(MainActivity.Hue_low));
        ((EditText)findViewById(R.id.InputSaturationLow)).setText(String.valueOf(MainActivity.Sat_low));
        ((EditText)findViewById(R.id.InputValueLow)).setText(String.valueOf(MainActivity.Val_low));
        ((EditText)findViewById(R.id.InputHueHigh)).setText(String.valueOf(MainActivity.Hue_high));
        ((EditText)findViewById(R.id.InputSaturationHigh)).setText(String.valueOf(MainActivity.Sat_high));
        ((EditText)findViewById(R.id.InputValueHigh)).setText(String.valueOf(MainActivity.Val_high));
        ((EditText)findViewById(R.id.InputALPHA)).setText(String.valueOf(MainActivity.alpha));
        ((EditText)findViewById(R.id.InputBETA)).setText(String.valueOf(MainActivity.beta));
        ((EditText)findViewById(R.id.InputA)).setText(String.valueOf(MainActivity.th_MinVal));
        ((EditText)findViewById(R.id.InputB)).setText(String.valueOf(MainActivity.th_MaxVal));
        ((EditText)findViewById(R.id.InputSubmatRadius)).setText(String.valueOf(MainActivity.submat_radius));
        ((EditText)findViewById(R.id.InputRadius_for_bright_points_search)).setText(String.valueOf(MainActivity.radius_for_bright_points_search));
        ((EditText)findViewById(R.id.InputEq_bright_level)).setText(String.valueOf(MainActivity.eq_bright_level));
    }
    int int_to_dp(int sizeInDp){
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (sizeInDp*scale + 0.5f);
        return dpAsPixels;
    }
}