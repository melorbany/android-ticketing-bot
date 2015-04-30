package com.ticket.media;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ChatActivity extends Activity {

    private ChatAdapter adapter;
    private EditText messageEditText;
    private ListView messagesContainer;
    private ProgressBar progressBar;
    private Button sendButton;
    public Device device;
    private String tempMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        device = new Device(this);

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
                tempMessage = messageText;

                messageEditText.setText("");
                showMessage(chatMessage);
                new TextMessageSender().execute();

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
        if (id == R.id.action_settings) {
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

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    private class TextMessageSender extends AsyncTask<Void, Integer, String> {
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
            return sendMessage();
        }

        @SuppressWarnings("deprecation")
        private String sendMessage() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(Config.INBOUND_MESSAGE);

            try {
//                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
//                        new AndroidMultiPartEntity.ProgressListener() {
//
//                            @Override
//                            public void transferred(long num) {
////                                publishProgress((int) ((num / (float) totalSize) * 100));
//                            }
//                        });

//                MultipartEntity entity = new MultipartEntity();

//                File sourceFile = new File(file.getAbsolutePath());


                // Adding file data to http body
//                entity.addPart("audio", new FileBody(sourceFile));
//                entity.addPart("email", new StringBody(device.getGoogleAccount()));
//                entity.addPart("device", new StringBody(device.getDeviceID()));
//                entity.addPart("title", new StringBody("Hellllo"));
//                entity.addPart("description", new StringBody("dhdjhhd"));
//
//                httpPost.setEntity(entity);

                System.out.println("tempMessage >>>>>>>>>>>>>> " + tempMessage);
                System.out.println("device.getDeviceID():: " + device.getDeviceID());
                System.out.println("device.getGoogleAccount():: " + device.getGoogleAccount());

                // Making server call
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("pT_SENDER", device.getDeviceID()));
                nameValuePairs.add(new BasicNameValuePair("pT_SENDER_NICKNAME", device.getGoogleAccount()));
                nameValuePairs.add(new BasicNameValuePair("pT_CONTENT", tempMessage));
                nameValuePairs.add(new BasicNameValuePair("pT_CONTENT_TYPE_T_I_V_L","T"));
                nameValuePairs.add(new BasicNameValuePair("pT_CAPTION", ""));
                nameValuePairs.add(new BasicNameValuePair("pT_DETECTED_LANG_AR_EN", "AR"));
                nameValuePairs.add(new BasicNameValuePair("pT_ONLINE_OFFLINE_O_F", "O"));
                nameValuePairs.add(new BasicNameValuePair("pT_PARTICIPANT", ""));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httpPost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    System.out.println("getContentCharSet >>>>>>>>>>>>>> " + EntityUtils.getContentCharSet(r_entity));
                    System.out.println("responseString >>>>>>>>>>>>>> " + responseString);
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

}
