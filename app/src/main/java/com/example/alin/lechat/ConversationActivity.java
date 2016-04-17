package com.example.alin.lechat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        String conversationType = intent.getStringExtra(HomeActivity.EXTRA_CONVERSATION_TYPE);
        String conversationPartner = intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME);
        if(conversationPartner == null) {
            //we are in a group conversation
            conversationPartner = "Anon";
        }

        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)){
            getSupportActionBar().setTitle(intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME));
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            getSupportActionBar().setTitle(intent.getStringExtra(HomeActivity.EXTRA_GROUP_NAME));
        }

        final ArrayList<Message> arrayOfMessages = new ArrayList<Message>();
//        arrayOfMessages.add(new Message(conversationPartner, "hi"));
//        arrayOfMessages.add(new Message(conversationPartner, "how are you?"));

        final ListView messageList = (ListView) findViewById(R.id.messageListView);
        final MessageAdapter messageAdapter = new MessageAdapter(this, arrayOfMessages);

        messageList.setAdapter(messageAdapter);

        final EditText messageToSendView = (EditText) findViewById(R.id.messageEditText);

        Button sendButton = (Button) findViewById(R.id.messageSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageToSend = messageToSendView.getText().toString();
                if(messageToSend.length() == 0){
                    return;
                }

                new HttpRequestTask().execute();

                GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.e("friend list", response.toString());
//                                Intent intent = new Intent(MainActivity.this,FriendsList.class);
//                                try {
//                                    JSONArray rawName = response.getJSONObject().getJSONArray("data");
//                                    intent.putExtra("jsondata", rawName.toString());
//                                    startActivity(intent);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }).executeAsync();


                arrayOfMessages.add(new Message("Alin", messageToSend));
                messageAdapter.notifyDataSetChanged();
                messageToSendView.setText("");
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


    private class HttpRequestTask extends AsyncTask<Void, Void, JSONMessage> {
        @Override
        protected JSONMessage doInBackground(Void... params) {
            try {
                final String url = "http://188.247.227.127:8080/message";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                JSONMessage jsonMessage = new JSONMessage("hello", new Date(), "1", "2", "alin", "asd");
                return restTemplate.postForObject(url, jsonMessage, JSONMessage.class);
//                return restTemplate.getForObject(url, JSONMessage.class);
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
//            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
//            TextView greetingContentText = (TextView) findViewById(R.id.content_value);
//            greetingIdText.setText(greeting.getId());
//            greetingContentText.setText(greeting.getContent());
        }
    }
}
