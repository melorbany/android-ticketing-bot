package com.ticket.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class ChatActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private ChatAdapter adapter;
    private EditText messageEditText;
    private ListView messagesContainer;
    private ProgressBar progressBar;
    private Button sendButton;
    public Device device;
    private String tempMessage;
    int msgType;

    private static final String TAG = "Audio";

    private ImageButton recordButton;
    private ImageButton stopButton;
    private ImageButton playButton;
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    public File audioFile;
    long totalSize = 0;
    String fileServerPath;
    Message selectedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        device = new Device(this);

        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        audioFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() +  "/" + System.currentTimeMillis() +  "audio-recorder-output.m4a");
//        recordButton = (Button) findViewById(R.id.chatRecordButton);
//
//        messageEditText.addTextChangedListener(new TextWatcher(){
//            public void afterTextChanged(Editable s) {
//                if(messageEditText.getText().length() > 0){
//                    sendButton.setVisibility(View.VISIBLE);
//                    recordButton.setVisibility(View.INVISIBLE);
//                }else{
//                    sendButton.setVisibility(View.INVISIBLE);
//                    recordButton.setVisibility(View.VISIBLE);
//                }
//            }
//            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
//            public void onTextChanged(CharSequence s, int start, int before, int count){
//            }
//        });

        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgType = 0;
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                // Send chat message
                //
                Message chatMessage = new Message();
                chatMessage.setData(messageText);
                chatMessage.setSender(true);
                chatMessage.setDateSent(new Date());
                tempMessage = messageText;

                messageEditText.setText("");
                showMessage(chatMessage);
                new TextMessageSender().execute();

                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);


            }
        });

        messagesContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                selectedMessage =(Message) (messagesContainer.getItemAtPosition(myItemInt));
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
        if (id == R.id.action_audio) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();
            View recordBarView = inflater.inflate(R.layout.record_bar, null);

            // set title
            alertDialogBuilder.setTitle("Record Your Message");

            // set dialog message
            alertDialogBuilder
                    .setView(inflater.inflate(R.layout.record_bar, null));

            recordButton = (ImageButton)recordBarView.findViewById(R.id.recordBarButton);
            stopButton = (ImageButton)recordBarView.findViewById(R.id.stopBarButton);
            playButton = (ImageButton)recordBarView.findViewById(R.id.playBarButton);

            alertDialogBuilder.setPositiveButton("Send",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity

                            // send audio here
                            if(audioFile.length() != 0){
                                msgType = 1;
                                // upload file to server then send
                                new FileUploader().execute();

                                // update list
                                Message audioMessage = new Message();
                                audioMessage.setData(audioFile.getAbsolutePath());
                                audioMessage.setSender(true);
                                audioMessage.setDateSent(new Date());
                                audioMessage.setType(1);
                                showMessage(audioMessage);
                                new TextMessageSender().execute();

                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    private class TextMessageSender extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                if(msgType == 0){
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT", tempMessage));
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT_TYPE_T_I_V_L","T"));
                }else if(msgType == 1){
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT", fileServerPath));
                    nameValuePairs.add(new BasicNameValuePair("pT_CONTENT_TYPE_T_I_V_L","V"));
                }
                nameValuePairs.add(new BasicNameValuePair("pT_CAPTION", ""));
                nameValuePairs.add(new BasicNameValuePair("pT_DETECTED_LANG_AR_EN", "AR"));
                nameValuePairs.add(new BasicNameValuePair("pT_ONLINE_OFFLINE_O_F", "O"));
                nameValuePairs.add(new BasicNameValuePair("pT_PARTICIPANT", ""));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httpPost);
                HttpEntity r_entity = response.getEntity();

                System.out.println("responseString before : " + responseString);

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

    public void delete(View v) {
        Log.d(TAG, "delete()");
        this.audioFile.delete();
        // update the buttons
        this.setButtonsEnabled(true, false, this.audioFile.exists());
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

    public void sendAudioMessage(View v){

    }

    private void setButtonsEnabled(boolean record, boolean stop, boolean play) {
        recordButton.setEnabled(record);
        stopButton.setEnabled(stop);
        playButton.setEnabled(play);
    }

    /**
     * Uploading the audioFile to server
     * */
    private class FileUploader extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(audioFile.getAbsolutePath());

                // Adding audioFile data to http body
                entity.addPart("audio", new FileBody(sourceFile));
                entity.addPart("email", new StringBody(device.getGoogleAccount()));
                entity.addPart("device", new StringBody(device.getDeviceID()));
                entity.addPart("title", new StringBody("Audio Track", Charset.forName("UTF-8")));
                entity.addPart("description", new StringBody("Audio Track", Charset.forName("UTF-8")));

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
            super.onPostExecute(result);
        }
    }

    private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void playFromList(View view){
        Log.d(TAG, "play()");
        File tobePlayed = new File(selectedMessage.getData());
        if (tobePlayed.exists()) {
            this.mediaPlayer = new MediaPlayer();
            try {
                this.mediaPlayer.setDataSource(tobePlayed.getAbsolutePath());
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
}
