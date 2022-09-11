package com.devhub.tweak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    //defining variables
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView text;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // In Activity's onCreate() for instance
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
  //      getSupportActionBar().hide();

        //Animations
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooke
        image = findViewById(R.id.imageView);
        text = findViewById(R.id.textView);

        image.setAnimation(topAnim);
        text.setAnimation(bottomAnim);


        Thread thread = new Thread(){
            public void run(){
                try{
                    // to run the given code
                    sleep(2000);
                }catch(Exception e){
                    // to check if any exceptions are there and report it
                    e.printStackTrace();
                }finally{
                    // code is written inside finally because even if there is an exception
                    // the catch block will report it and the code further written in this finally block will be executed after it

                    // Here we want to move to the next screen after the splash screen is shown using intent
                    Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };thread.start();
    }

}