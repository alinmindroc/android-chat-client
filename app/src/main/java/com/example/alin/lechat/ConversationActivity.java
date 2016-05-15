package com.example.alin.lechat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import JSON_objects.JSONGroupMessage;
import JSON_objects.JSONMessage;

public class ConversationActivity extends AppCompatActivity {

    String currentUserId;
    String currentUserName;

    String conversationPartnerId;
    String conversationPartnerName;

    String currentGroupId;
    String currentGroupName;

    String conversationType;

    // Create the Handler object (on the main thread by default)
    final Handler handler = new Handler();
    Runnable runnablePrivateConversation = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            new HttpRequestGetMessages().execute(currentUserId, conversationPartnerId);
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnablePrivateConversation, 2000);
        }
    };

    Runnable runnableGroupConversation = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            new HttpRequestGetGroupMessages().execute(currentGroupId);
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableGroupConversation, 2000);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Don't use the onCreate method, because we want to remove the handler even when the application is paused (minimized)
        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)) {
            handler.post(runnablePrivateConversation);
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            handler.post(runnableGroupConversation);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Don't use the onCreate method, because we want to remove the handler even when the application is paused (minimized)
        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)) {
            handler.removeCallbacks(runnablePrivateConversation);
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            handler.removeCallbacks(runnableGroupConversation);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't use the onCreate method, because we want to remove the handler even when the application is paused (minimized)
        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)) {
            handler.removeCallbacks(runnablePrivateConversation);
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            handler.removeCallbacks(runnableGroupConversation);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        conversationType = intent.getStringExtra(HomeActivity.EXTRA_CONVERSATION_TYPE);
        currentUserName = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_NAME);
        currentUserId = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID);

        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)){
            conversationPartnerName = intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME);
            conversationPartnerId = intent.getStringExtra(HomeActivity.EXTRA_FRIEND_ID);
            getSupportActionBar().setTitle(conversationPartnerName);
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            currentGroupId = intent.getStringExtra(HomeActivity.EXTRA_GROUP_ID);
            currentGroupName = intent.getStringExtra(HomeActivity.EXTRA_GROUP_NAME);
            getSupportActionBar().setTitle(currentGroupName);
        }

        final ArrayList<Message> arrayOfMessages = new ArrayList<Message>();

        final ListView messageList = (ListView) findViewById(R.id.messageListView);
        final MessageAdapter messageAdapter = new MessageAdapter(this, arrayOfMessages);

        messageList.setAdapter(messageAdapter);

        final EditText messageInput = (EditText) findViewById(R.id.groupNameEditText);

        Button sendButton = (Button) findViewById(R.id.messageSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = messageInput.getText().toString();
                if (content.length() == 0) {
                    return;
                }

                if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)){
                    //create a new message object and send it to the server
                    JSONMessage jsonMessage = new JSONMessage(content, new Date(), currentUserId, conversationPartnerId, currentUserName, conversationPartnerName);

                    new HttpRequestSendMessage().execute(jsonMessage);
                } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
                    //create a new message object and send it to the server
                    JSONGroupMessage jsonGroupMessage = new JSONGroupMessage(content, currentUserId, currentUserName, currentGroupId);

                    new HttpRequestSendGroupMessage().execute(jsonGroupMessage);
                }

                messageInput.setText("");
            }
        });
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

                    final EditText messageEditText = (EditText)findViewById(R.id.groupNameEditText);
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
                TextView tvText = (TextView) convertView.findViewById(R.id.messageTextOther);
                TextView tvUserName = (TextView) convertView.findViewById(R.id.usernameTextOther);

                tvText.setText(message.messageText);
                tvUserName.setText(message.senderName);
            }

            return convertView;
        }
    }

    private class HttpRequestSendMessage extends AsyncTask<JSONMessage, Void, String> {
        @Override
        protected String doInBackground(JSONMessage... params) {
            try {
                final String url = "http://188.247.227.127:8080/message";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.postForObject(url, params[0], String.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String greeting) {
            if(greeting == null){
                return;
            }
            Log.e("asd", greeting);
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
                arrayOfMessages.add(new Message(m.get("senderName").toString(), currentUserName, m.get("text").toString()));
            }

            messageAdapter.notifyDataSetChanged();
        }
    }

    private class HttpRequestSendGroupMessage extends AsyncTask<JSONGroupMessage, Void, String> {
        @Override
        protected String doInBackground(JSONGroupMessage... params) {
            try {
                final String url = "http://188.247.227.127:8080/groupMessage";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.postForObject(url, params[0], String.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String greeting) {
            if(greeting == null){
                return;
            }
            Log.e("asd", greeting);
        }
    }

    private class HttpRequestGetGroupMessages extends AsyncTask<String, Void, List<LinkedHashMap>> {
        @Override
        protected List<LinkedHashMap> doInBackground(String... params) {
            try {
                final String url = "http://188.247.227.127:8080";

                String targetUrl= UriComponentsBuilder.fromUriString(url)
                        .path("/groupMessage")
                        .queryParam("groupId", params[0])
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
                arrayOfMessages.add(new Message(m.get("senderName").toString(), currentUserName, m.get("text").toString()));
            }

            messageAdapter.notifyDataSetChanged();
        }
    }
}
