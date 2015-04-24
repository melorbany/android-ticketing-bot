package com.ticket.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
public class SplashScreen extends Activity {

    boolean isAutoLogin = false;
    Intent intent;
    TextView textView;
    ImageView imgLogo;
    // Splash screen timer
    private int splashTime = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        textView = (TextView) findViewById(R.id.splashstatus);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);

//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                imgLogo.setImageResource(R.drawable.si0);
//            }
//        }, 1000);
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//               imgLogo.setImageResource(R.drawable.si1);
//            }
//        }, 1000);
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                imgLogo.setImageResource(R.drawable.si2);
//            }
//        }, 1000);

        intent = new Intent(SplashScreen.this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {

			/*
             * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                //StartAnimations();
                imgLogo.setImageResource(R.drawable.sp1);
                startActivity(intent);
                // close this activity
                finish();
            }
        }, splashTime);
    }

}
