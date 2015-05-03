package com.ticket.media;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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

                if(message.getType()==1) {
//                    Intent intent = new Intent();
//                    intent.setClass(ChatActivity.this, ImageDetailActivity.class);
//
//                    intent.putExtra("EXTRA_IMAGE", message.getPath());
//
//                    // the sample activity
//                    startActivity(intent);

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" +message.getPath()), "image/*");
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
                //
                Message chatMessage = new Message();
                chatMessage.setData(messageText);
                chatMessage.setSender(true);
                chatMessage.setDateSent(new Date());
                chatMessage.setType(3);

                messageEditText.setText("");
                showMessage(chatMessage);
                new UploadFileToServer(chatMessage).execute();

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

                    Message message =new Message();
                    message.setType(1);
                    message.setUri(imageUri);
                    String filePath = getRealPathFromURI(imageUri);
                    message.setPath(filePath);
                    message.setSender(true);
                    message.setData(filePath);
                    message.setDateSent(new Date());
                    showMessage(message);
                    new UploadFileToServer(message).execute();

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

        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        Message message;
        private UploadFileToServer(Message message) {
            this.message = message;
        }

        public  ProgressDialog createProgressDialog(Context mContext) {
            ProgressDialog dialog = new ProgressDialog(mContext);
            try {
                dialog.show();
            } catch (WindowManager.BadTokenException e) {

            }
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.progress_dialog);
            // dialog.setMessage(Message);
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
//                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
//                        new AndroidMultiPartEntity.ProgressListener() {
//
//                            @Override
//                            public void transferred(long num) {
//                            }
//                        });
//
//                entity.addPart("email", new StringBody(device.getGoogleAccount()));
//                entity.addPart("device", new StringBody(device.getDeviceID()));
//                entity.addPart("description", new StringBody(this.message.getData()));

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                if(this.message.getType()!=3) {
                    File file = new File(this.message.getPath());
                    FileBody fb = new FileBody(file);
                    builder.addPart("file", fb);
                }

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

                returnValue = jsonObj.getString("response");
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
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            super.onPostExecute(result);
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

}
