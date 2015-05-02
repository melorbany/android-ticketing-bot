package com.ticket.media;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.ticket.helpers.Image;


public class ImageDetailActivity extends Activity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);


        String imagePath = getIntent().getStringExtra("EXTRA_IMAGE");
        imageView = (ImageView)findViewById(R.id.imageView);
        Image image = new Image();
        Bitmap bi = image.decodeSampledBitmapFromFile(imagePath,1000,1000);

        imageView.setImageBitmap(bi);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
