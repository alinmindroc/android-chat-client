package com.example.alin.lechat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    String currentUserName;
    String conversationPartnerId;
    String currentUserId;

    // Create the Handler object (on the main thread by default)
    final Handler handler = new Handler();
    // Define the code block to be executed
    Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            new HttpRequestGetMessages().execute(currentUserId, conversationPartnerId);
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        String conversationType = intent.getStringExtra(HomeActivity.EXTRA_CONVERSATION_TYPE);
        final String conversationPartnerName = intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME);
        conversationPartnerId = intent.getStringExtra(HomeActivity.EXTRA_FRIEND_ID);

        currentUserName = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_NAME);
        currentUserId = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID);

        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)){
            getSupportActionBar().setTitle(intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME));
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            getSupportActionBar().setTitle(intent.getStringExtra(HomeActivity.EXTRA_GROUP_NAME));
        }

        final ArrayList<Message> arrayOfMessages = new ArrayList<Message>();

        final ListView messageList = (ListView) findViewById(R.id.messageListView);
        final MessageAdapter messageAdapter = new MessageAdapter(this, arrayOfMessages);

        messageList.setAdapter(messageAdapter);

        final EditText messageInput = (EditText) findViewById(R.id.messageEditText);

        Button sendButton = (Button) findViewById(R.id.messageSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = messageInput.getText().toString();
                if (content.length() == 0) {
                    return;
                }

                //create a new message object and send it to the server
                JSONMessage jsonMessage = new JSONMessage(content, new Date(), currentUserId, conversationPartnerId, currentUserName, conversationPartnerName);

                new HttpRequestSendMessage().execute(jsonMessage);

//                arrayOfMessages.add(new Message(currentUserName, content));
//                messageAdapter.notifyDataSetChanged();
                messageInput.setText("");
            }
        });

        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);

//        new HttpRequestGetMessages().execute(currentUserId, conversationPartnerId);
    }

    private final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("URI", "File Uri: " + uri.toString());

                    final EditText messageEditText = (EditText)findViewById(R.id.messageEditText);
                    messageEditText.setText(uri.getLastPathSegment().toString());
                    // Get the path
                    String path = null;
                    try {
                        path = getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.d("PATH", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            showFileChooser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MessageAdapter extends ArrayAdapter<Message> {
        ArrayList<Message> users;
        private final int CRT_USER_MESSAGE_TYPE = 1;
        private final int OTHER_USER_MESSAGE_TYPE = 0;

        public MessageAdapter(Context context, ArrayList<Message> users) {
            super(context, 0, users);
            this.users = users;
        }

        public int getItemViewType(int position) {
            if(users.get(position).isCurrentUser()){
                return CRT_USER_MESSAGE_TYPE;
            } else {
                return OTHER_USER_MESSAGE_TYPE;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Count of different layouts
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = getItem(position);

            LayoutInflater lInflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == CRT_USER_MESSAGE_TYPE) {
                convertView = lInflater.inflate(R.layout.message_list_entry_user, parent, false);
                TextView tvName = (TextView) convertView.findViewById(R.id.messageTextCrt);
                tvName.setText(message.messageText);
            } else {
                convertView = lInflater.inflate(R.layout.message_list_entry_other, parent, false);
                TextView tvName = (TextView) convertView.findViewById(R.id.messageTextOther);
                tvName.setText(message.messageText);
            }

            return convertView;
        }
    }


    private class HttpRequestSendMessage extends AsyncTask<JSONMessage, Void, JSONMessage> {
        @Override
        protected JSONMessage doInBackground(JSONMessage... params) {
            try {
                final String url = "http://188.247.227.127:8080/message";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.postForObject(url, params[0], JSONMessage.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONMessage greeting) {
            if(greeting == null){
                return;
            }
            Log.e("asd", greeting.toString());
        }
    }

    private class HttpRequestGetMessages extends AsyncTask<String, Void, List<LinkedHashMap>> {
        @Override
        protected List<LinkedHashMap> doInBackground(String... params) {
            try {
                final String url = "http://188.247.227.127:8080";

                String targetUrl= UriComponentsBuilder.fromUriString(url)
                        .path("/message")
                        .queryParam("receiverId", params[0])
                        .queryParam("senderId", params[1])
                        .build()
                        .toUri()
                        .toString();

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return (List<LinkedHashMap>) restTemplate.getForObject(targetUrl, List.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<LinkedHashMap> messages) {
            if(messages == null){
                return;
            }
            Log.e("asd", messages.toString());
            ArrayList<Message> arrayOfMessages = new ArrayList<Message>();

            ListView messageList = (ListView) findViewById(R.id.messageListView);
            MessageAdapter messageAdapter = new MessageAdapter(ConversationActivity.this, arrayOfMessages);

            messageList.setAdapter(messageAdapter);

            for(LinkedHashMap m: messages){
                arrayOfMessages.add(new Message(currentUserName, m.get("senderName").toString(), m.get("text").toString()));
            }

            messageAdapter.notifyDataSetChanged();
        }
    }

}
