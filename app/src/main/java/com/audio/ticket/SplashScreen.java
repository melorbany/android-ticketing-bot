package com.audio.ticket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
public class SplashScreen extends Activity {

    boolean isAutoLogin = false;
    Intent intent;
    TextView textView;
    // Splash screen timer
    private int splashTime = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        textView = (TextView) findViewById(R.id.splashstatus);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
//                textView.setText(R.string.retrieve_data);
            }
        }, 3000);

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
                startActivity(intent);

                // close this activity
                finish();
            }
        }, splashTime);
    }

}
