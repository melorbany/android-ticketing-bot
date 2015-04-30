package com.ticket.media;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ticket.helpers.Camera;
import com.ticket.helpers.ChatAdapter;
import com.ticket.helpers.Config;
import com.ticket.helpers.Device;
import com.ticket.helpers.Message;

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
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ChatActivity extends Activity {

    private ChatAdapter adapter;
    private EditText messageEditText;
    private ListView messagesContainer;
    private ProgressBar progressBar;
    private Button sendButton;
    public Device device;
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        device = new Device(this);
        camera = new Camera(this);

        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);

        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                chatMessage.setType(3);

                messageEditText.setText("");
                showMessage(chatMessage);
                new UploadFileToServer().execute();

                InputMethodManager imm = (InputMethodManager)getSystemService(
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
        }else if (id == R.id.action_video){

            return true;
        }else if(id == R.id.action_audio){

            return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Camera.ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    Uri imageUri = camera.handleBigCameraPhoto();

                    Message message =new Message();
                    message.setType(1);
                    message.setUri(imageUri);
                    message.setSender(true);
                    message.setDateSent(new Date());
                    showMessage(message);
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
                            }
                        });

                entity.addPart("email", new StringBody(device.getGoogleAccount()));
                entity.addPart("device", new StringBody(device.getDeviceID()));
                entity.addPart("title", new StringBody("Hellllo"));
                entity.addPart("description", new StringBody("dhdjhhd"));

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

            String returnValue ="";
            try {
                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj == null)
                    return;

                returnValue = jsonObj.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(returnValue !="") {
                Message chatMessage = new Message();
                chatMessage.setData(returnValue + "");
                chatMessage.setSender(false);
                chatMessage.setDateSent(new Date());
                chatMessage.setType(3);

                showMessage(chatMessage);
            }
            super.onPostExecute(result);
        }

    }


    /**
     * Fragment that appears in the "content_frame", shows a planet
     */


}
