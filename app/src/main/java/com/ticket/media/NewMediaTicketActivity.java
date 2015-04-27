
package com.ticket.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ticket.camera.AlbumStorageDirFactory;
import com.ticket.camera.BaseAlbumDirFactory;
import com.ticket.camera.FroyoAlbumDirFactory;
import com.ticket.helpers.Config;
import com.ticket.helpers.Device;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewMediaTicketActivity extends Activity implements OnCompletionListener {


    private static final String OUT_FILE_NAME = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/audio-recorder-output.m4a";

    private ImageButton recordButton;
    private ImageButton stopButton;
    private ImageButton playButton;

    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    public File file;

    public EditText title;
    public EditText description;
    private Button btnUpload;

    public String []imagesFilePath;
    public String []videoFilePath;

    public int imagesFileCount =0;
    public int videoFileCount =0;

    public Device device;
    long totalSize = 0;


    private static final String TAG = "Audio";
    private static final int ACTION_TAKE_PHOTO_B = 1;
    private static final int ACTION_TAKE_PHOTO_S = 2;
    private static final int ACTION_TAKE_VIDEO = 3;

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
//    private ImageView mImageView;
    private Bitmap mImageBitmap;

    private static final String VIDEO_STORAGE_KEY = "viewvideo";
    private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
//    private VideoView mVideoView;
    private Uri mVideoUri;

    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void setPic() {

//		/* There isn't enough memory to open up more than a couple camera photos */
//		/* So pre-scale the target bitmap into which the file is decoded */
//
//		/* Get the size of the ImageView */
//        int targetW = mImageView.getWidth();
//        int targetH = mImageView.getHeight();
//
//		/* Get the size of the image */
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//		/* Figure out which way needs to be reduced less */
//        int scaleFactor = 1;
//        if ((targetW > 0) || (targetH > 0)) {
//            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//        }
//
//		/* Set bitmap options to scale the image decode target */
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;
//
//		/* Decode the JPEG file into a Bitmap */
//        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//
//		/* Associate the Bitmap to the ImageView */
//        mImageView.setImageBitmap(bitmap);
//        mVideoUri = null;
//        mImageView.setVisibility(View.VISIBLE);
//        mVideoView.setVisibility(View.INVISIBLE);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        imagesFilePath[imagesFileCount++] = mCurrentPhotoPath;
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    Log.v("Image2",mCurrentPhotoPath);

                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
    }

    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
//        mImageBitmap = (Bitmap) extras.get("data");
//        mImageView.setImageBitmap(mImageBitmap);
//        mVideoUri = null;
//        mImageView.setVisibility(View.VISIBLE);
//        mVideoView.setVisibility(View.INVISIBLE);
    }

    private void handleBigCameraPhoto() {
        Log.v("Image1",mCurrentPhotoPath);
        if (mCurrentPhotoPath != null) {
//            setPic();

            galleryAddPic();
            mCurrentPhotoPath = null;
        }

    }

    private void handleCameraVideo(Intent intent) {
        mVideoUri = intent.getData();
        String path = getRealPathFromURI(this,mVideoUri);
        videoFilePath[videoFileCount++] = path;

//        mVideoView.setVideoURI(mVideoUri);
//        mImageBitmap = null;
//        mVideoView.setVisibility(View.VISIBLE);
//        mImageView.setVisibility(View.INVISIBLE);
    }

    Button.OnClickListener mTakePicOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }
            };

    Button.OnClickListener mTakePicSOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
                }
            };

    Button.OnClickListener mTakeVidOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakeVideoIntent();
                }
            };

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.activity_new_media_ticket);
        this.recordButton = (ImageButton)super.findViewById(R.id.recordButton);
        this.stopButton = (ImageButton)super.findViewById(R.id.stopButton);
        this.playButton = (ImageButton)super.findViewById(R.id.playButton);
//        this.deleteButton = (ImageButton)super.findViewById(R.id.deleteButton);
        this.file = new File(OUT_FILE_NAME);
        this.title = (EditText)super.findViewById(R.id.title);
        this.description = (EditText)super.findViewById(R.id.description);
        this.setButtonsEnabled(true, false, this.file.exists());


        device = new Device(this);
//        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnMediaUpload);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imagesFilePath = new String[50];
        videoFilePath = new String[50];

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server

                final ProgressDialog dialog = ProgressDialog.show(NewMediaTicketActivity.this, "", "Sending to server ....", true);
                dialog.show();

                Thread initializer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new UploadFileToServer().execute();
                        dialog.dismiss();
                    }
                });
                initializer.start();


            }
        });


        ImageButton picBtn = (ImageButton) findViewById(R.id.btnIntend);
        setBtnListenerOrDisable(
                picBtn,
                mTakePicOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );


        ImageButton vidBtn = (ImageButton) findViewById(R.id.btnIntendV);
        setBtnListenerOrDisable(
                vidBtn,
                mTakeVidOnClickListener,
                MediaStore.ACTION_VIDEO_CAPTURE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case ACTION_TAKE_PHOTO_S: {
                if (resultCode == RESULT_OK) {
                    handleSmallCameraPhoto(data);
                }
                break;
            } // ACTION_TAKE_PHOTO_S

            case ACTION_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
                    handleCameraVideo(data);
                }
                break;
            } // ACTION_TAKE_VIDEO
        } // switch
    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
        outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null) );
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
//        mImageView.setImageBitmap(mImageBitmap);
//        mImageView.setVisibility(
//                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
//                        ImageView.VISIBLE : ImageView.INVISIBLE
//        );
//        mVideoView.setVideoURI(mVideoUri);
//        mVideoView.setVisibility(
//                savedInstanceState.getBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY) ?
//                        ImageView.VISIBLE : ImageView.INVISIBLE
//        );
    }


    private void setButtonsEnabled(boolean record, boolean stop, boolean playAndDelete) {
        this.recordButton.setEnabled(record);
        this.stopButton.setEnabled(stop);
        this.playButton.setEnabled(playAndDelete);
//        this.deleteButton.setEnabled(playAndDelete);
    }

    public void record(View v) {
        Log.d(TAG, "record");
        this.mediaRecorder = new MediaRecorder();
        this.mediaRecorder.setAudioChannels(1);
        this.mediaRecorder.setAudioSamplingRate(44100);
        this.mediaRecorder.setAudioEncodingBitRate(64000);
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        this.mediaRecorder.setOutputFile(this.file.getAbsolutePath());
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            this.mediaRecorder.prepare();
            this.mediaRecorder.start();

            // update the buttons
            this.setButtonsEnabled(false, true, false);
        } catch (IOException e) {
            Log.e(TAG, "Failed to record()", e);
        }
    }

    public void play(View v) {
        Log.d(TAG, "play()");
        if (this.file.exists()) {
            this.mediaPlayer = new MediaPlayer();
            try {
                this.mediaPlayer.setDataSource(this.file.getAbsolutePath());
                this.mediaPlayer.prepare();
                this.mediaPlayer.setOnCompletionListener(this);
                this.mediaPlayer.start();

                // update the buttons
                this.setButtonsEnabled(false, true, false);
            } catch (IOException e) {
                Log.e(TAG, "Failed to play()", e);
            }
        } else {
            this.playButton.setEnabled(false);
        }
    }

    public void stop(View v) {
        Log.d(TAG, "stop()");
        if (this.mediaPlayer != null) {
            // stop/release the media player
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        } else if (this.mediaRecorder != null) {
            // stop/release the media recorder
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            this.mediaRecorder = null;
        }
        // update the buttons
        this.setButtonsEnabled(true, false, this.file.exists());
    }

    public void delete(View v) {
        Log.d(TAG, "delete()");
        this.file.delete();
        // update the buttons
        this.setButtonsEnabled(true, false, this.file.exists());
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stop(null);
    }

    // called when the playback is done
    public void onCompletion(MediaPlayer mp) {
        this.stop(null);
    }


    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
//            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
//            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
//            progressBar.setProgress(progress[0]);

            // updating percentage value
//            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);


            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

//                MultipartEntity entity = new MultipartEntity();

                File sourceFile = new File(file.getAbsolutePath());


                // Adding file data to http body
                entity.addPart("audio", new FileBody(sourceFile));
                entity.addPart("email", new StringBody(device.getGoogleAccount()));
                entity.addPart("device", new StringBody(device.getDeviceID()));
                entity.addPart("title", new StringBody(title.getText().toString(), Charset.forName("UTF-8")));
                entity.addPart("description", new StringBody(description.getText().toString(), Charset.forName("UTF-8")));

                for (int i=0;i<imagesFileCount;i++){

                    sourceFile = new File(imagesFilePath[i]);
                    InputStream is = new FileInputStream(sourceFile);
//                    byte[] data = IOUtils.toByteArray(is);
//                    entity.addPart("image", new FileBody(sourceFile));
                }

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            } catch (Exception e){
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            int returnValue =0;
            try {
                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj == null)
                    return;

                returnValue = Integer.parseInt(jsonObj.getString("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String message = "Ticket successfully sent.\nYour Ticket ID : "+returnValue;
            // showing the server response in an alert dialog
            showAlert(message);

            super.onPostExecute(result);
        }

    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Audio Ticket")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setBtnListenerOrDisable(
            ImageButton btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setClickable(false);
        }
    }



    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
