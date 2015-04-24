
package com.ticket.media;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

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

public class NewTicketActivity extends Activity implements OnCompletionListener {
    private static final String TAG = "Audio";

    private static final String OUT_FILE_NAME = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/audio-recorder-output.m4a";

    private ImageButton recordButton;
    private ImageButton stopButton;
    private ImageButton playButton;

//    private ImageButton deleteButton;

    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    public File file;

//    private ProgressBar progressBar;
//    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    public EditText title;
    public EditText description;
    private Button btnUpload;
    public Device device;
    long totalSize = 0;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.activity_new_ticket);
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
        btnUpload = (Button) findViewById(R.id.btnUpload);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server

                final ProgressDialog dialog = ProgressDialog.show(NewTicketActivity.this, "", "Sending to server ....", true);
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

}
