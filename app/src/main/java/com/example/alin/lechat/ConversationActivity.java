package com.example.alin.lechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ConversationActivity extends AppCompatActivity {

    private String PRIVATE_CONVERSATION_TITLE = "Private conversation with ";
    private String GROUP_CONVERSATION_TITLE = "Group conversation: ";

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
            getSupportActionBar().setTitle(PRIVATE_CONVERSATION_TITLE + intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME));
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            getSupportActionBar().setTitle(GROUP_CONVERSATION_TITLE + intent.getStringExtra(HomeActivity.EXTRA_GROUP_NAME));
        }

        final ArrayList<Message> arrayOfMessages = new ArrayList<Message>();
        arrayOfMessages.add(new Message(conversationPartner, "hi"));
        arrayOfMessages.add(new Message(conversationPartner, "how are you?"));

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

                arrayOfMessages.add(new Message("Alin", messageToSend));
                messageAdapter.notifyDataSetChanged();
                messageToSendView.setText("");
            }
        });
    }

    class Message {
        public String userName;
        public String messageText;

        public Message(String userName, String messageText) {
            this.userName = userName;
            this.messageText = messageText;
        }

        public boolean isCurrentUser(){
            return userName.equals("Alin");
        }
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



}
