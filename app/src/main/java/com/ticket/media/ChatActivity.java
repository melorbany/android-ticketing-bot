package com.ticket.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ticket.helpers.BaseActivity;
import com.ticket.helpers.Camera;
import com.ticket.helpers.ChatAdapter;
import com.ticket.helpers.Config;
import com.ticket.helpers.Device;
import com.ticket.helpers.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class ChatActivity extends BaseActivity implements MediaPlayer.OnCompletionListener {

    private ChatAdapter adapter;
    private EditText messageEditText;
    private ListView messagesContainer;
    private ProgressBar progressBar;
    private Button sendButton;
    public Device device;
    Camera camera;
    private static final String TAG = "Audio";
    private ImageButton recordButton;
    private ImageButton stopButton;
    private ImageButton playButton;
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    public File audioFile;
    long totalSize = 0;
    String audioFilePath;
    String imageFilePath;
    ArrayList<Message> messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        device = new Device(this);
        camera = new Camera(this);
        messagesList = new ArrayList<Message>();
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);

        messagesContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Message message = adapter.chatMessages.get(position);

                if (message.getType() == Config.MESSAGE_TYPE_IMAGE) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + message.getPath()), "image/*");
                    startActivity(intent);
                }else if(message.getType() == Config.MESSAGE_TYPE_LOCATION) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f",
                            message.getLocation().getLatitude(),
                            message.getLocation().getLongitude());

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                // Send chat message
                Message chatMessage = new Message();
                chatMessage.setData(messageText);
                chatMessage.setSender(true);
                chatMessage.setDateSent(new Date());
                chatMessage.setType(Config.MESSAGE_TYPE_TEXT);

                messageEditText.setText("");
                showMessage(chatMessage);
                //new UploadFileToServer(chatMessage).execute();
                new MessageSender(chatMessage).execute();

                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_img) {
            if (camera.isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE)) {
                Intent takePictureIntent = camera.dispatchTakePictureIntent();
                startActivityForResult(takePictureIntent, Camera.ACTION_TAKE_PHOTO_B);
            }
            return true;
        } else if (id == R.id.action_video) {
            return true;
        }
        else if (id == R.id.action_location) {
            startUpdate();
        }
        else if (id == R.id.action_audio) {
            audioFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/a_" + System.currentTimeMillis() + ".m4a");

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();
            View recordBarView = inflater.inflate(R.layout.record_bar, null);

            // set title
            alertDialogBuilder.setTitle("Record Your Message");

            // set dialog message
            alertDialogBuilder
                    .setView(inflater.inflate(R.layout.record_bar, null));

            recordButton = (ImageButton) recordBarView.findViewById(R.id.recordBarButton);
            stopButton = (ImageButton) recordBarView.findViewById(R.id.stopBarButton);
            playButton = (ImageButton) recordBarView.findViewById(R.id.playBarButton);

            alertDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity

                    // send audio here
                    if (audioFile.length() != 0) {
                        // update list
                        Message audioMessage = new Message();
                        audioMessage.setData("Audio Message Sent");
                        audioMessage.setSender(true);
                        audioMessage.setDateSent(new Date());
                        audioMessage.setType(Config.MESSAGE_TYPE_AUDIO);
                        audioMessage.setPath(audioFile.getAbsolutePath());
                        showMessage(audioMessage);
                        new UploadFileToServer(audioMessage).execute();
                        new MessageSender(audioMessage).execute();

                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
                    }
                    dialog.dismiss();
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    public void showMessage(Message message) {
        adapter.add(message);
        messagesList.add(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Camera.ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    Uri imageUri = camera.handleBigCameraPhoto();

                    Message message = new Message();
                    message.setType(Config.MESSAGE_TYPE_IMAGE);
                    message.setUri(imageUri);
                    String filePath = getRealPathFromURI(imageUri);
                    message.setPath(filePath);
                    message.setSender(true);
                    message.setData(filePath);
                    message.setDateSent(new Date());
                    showMessage(message);
                    new UploadFileToServer(message).execute();
                    new MessageSender(message).execute();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case Camera.ACTION_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
//                    handleCameraVideo(data);
                }
                break;
            } // ACTION_TAKE_VIDEO
        } // switch
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {

        /**
         * progress dialog to show user that the backup is processing.
         */
        private ProgressDialog dialog;
        Message message;

        private UploadFileToServer(Message message) {
            this.message = message;
        }

        public ProgressDialog createProgressDialog(Context mContext) {
            ProgressDialog dialog = new ProgressDialog(mContext);
            try {
                dialog.show();
            } catch (WindowManager.BadTokenException e) {

            }
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.progress_dialog);
            return dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (this.dialog == null) {
                this.dialog = createProgressDialog(ChatActivity.this);
                this.dialog.show();
            } else {
                this.dialog.show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
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
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                //Upload file
                FileBody fb = null;
                if (this.message.getType() == Config.MESSAGE_TYPE_IMAGE) {
                    fb = new FileBody(new File(this.message.getPath()));
                } else if (this.message.getType() == Config.MESSAGE_TYPE_AUDIO) {
                    File sourceFile = new File(audioFile.getAbsolutePath());
                    fb = new FileBody(sourceFile);
                }

                builder.addPart("file", fb);
                builder.addPart("email", new StringBody(device.getGoogleAccount()));
                builder.addPart("device", new StringBody(device.getDeviceID()));
                builder.addPart("description", new StringBody(this.message.getData()));

                final HttpEntity builderEntity = builder.build();
                httppost.setEntity(builderEntity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    if (this.message.getType() == Config.MESSAGE_TYPE_IMAGE) {
                        imageFilePath = responseString;
                    } else if (this.message.getType() == Config.MESSAGE_TYPE_AUDIO) {
                        audioFilePath = responseString;
                    }
                } else {
                    responseString = "Error Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            } catch (Exception e) {
                responseString = e.toString();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {

            String returnValue = "";
            try {
                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj == null)
                    return;

                returnValue = jsonObj.getString("response");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (returnValue != "") {
                Message chatMessage = new Message();
                chatMessage.setData(returnValue + "");
                chatMessage.setSender(false);
                chatMessage.setDateSent(new Date());
                chatMessage.setType(Config.MESSAGE_TYPE_TEXT);
                //showMessage(chatMessage);
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            super.onPostExecute(result);
        }

    }


    /**
     * Callback that fires when the location changes.
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        if(changeCount > 1){
            stopUpdate();
            Message message = new Message();
            message.setType(Config.MESSAGE_TYPE_LOCATION);
            message.setLocation(mCurrentLocation);
            message.setData(location.getLatitude() + "," + location.getLongitude());
            message.setDateSent(new Date());
            showMessage(message);
        }

    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class MessageSender extends AsyncTask<Void, Integer, String> {
        Message message;

        private MessageSender() {
        }

        private MessageSender(Message theMessage) {
            this.message = theMessage;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected String doInBackground(Void... params) {
            return sendMessage();
        }

        @SuppressWarnings("deprecation")
        private String sendMessage() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(Config.INBOUND_MESSAGE);

            try {
                // Making server call
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("pT_SENDER", device.getDeviceID()));
                nameValuePairs.add(new BasicNameValuePair("pT_SENDER_NICKNAME", device.getGoogleAccount()));
                if (message.getType() == Config.MESSAGE_TYPE_TEXT) {
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT", message.getData()));
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT_TYPE_T_I_V_L", "T"));
                } else if (message.getType() == Config.MESSAGE_TYPE_AUDIO) {
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT", audioFilePath));
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT_TYPE_T_I_V_L", "V"));
                } else if (message.getType() == Config.MESSAGE_TYPE_IMAGE) {
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT", imageFilePath));
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT_TYPE_T_I_V_L", "I"));
                }
                nameValuePairs.add(new BasicNameValuePair("pT_CAPTION", ""));
                nameValuePairs.add(new BasicNameValuePair("pT_DETECTED_LANG_AR_EN", "AR"));
                nameValuePairs.add(new BasicNameValuePair("pT_ONLINE_OFFLINE_O_F", "O"));
                nameValuePairs.add(new BasicNameValuePair("pT_PARTICIPANT", ""));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                HttpResponse response = httpclient.execute(httpPost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    responseString = responseString.replaceAll("&lt;", "<");
                    responseString = responseString.replaceAll("&gt;", ">");
                    Document doc = convertStringToDocument(responseString);
                    responseString = doc.getElementsByTagName("en").item(0).getTextContent();
                } else {
                    responseString = "Error Status Code: "
                            + statusCode;
                }

            } catch (HttpHostConnectException e) {
                responseString = "No Internet Connection";
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            } catch (Exception e) {
                responseString = e.toString();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.equals(""))
                return;


            if (result != "") {
                Message chatMessage = new Message();
                chatMessage.setData(result + "");
                chatMessage.setSender(false);
                chatMessage.setDateSent(new Date());
                showMessage(chatMessage);
            }
            super.onPostExecute(result);
        }

    }

    public void record(View v) {
        if (recordButton.isEnabled()) {
            Log.d(TAG, "record");
            this.mediaRecorder = new MediaRecorder();
            this.mediaRecorder.setAudioChannels(1);
            this.mediaRecorder.setAudioSamplingRate(44100);
            this.mediaRecorder.setAudioEncodingBitRate(64000);
            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            this.mediaRecorder.setOutputFile(this.audioFile.getAbsolutePath());
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
    }

    public void play(View v) {
        Log.d(TAG, "play()");
        if (this.audioFile.exists()) {
            this.mediaPlayer = new MediaPlayer();
            try {
                this.mediaPlayer.setDataSource(this.audioFile.getAbsolutePath());
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
        this.setButtonsEnabled(true, false, this.audioFile.exists());
    }

    private void setButtonsEnabled(boolean record, boolean stop, boolean play) {
        recordButton.setEnabled(record);
        stopButton.setEnabled(stop);
        playButton.setEnabled(play);
    }

    // called when the playback is done
    public void onCompletion(MediaPlayer mp) {
        this.stop(null);
    }
}
