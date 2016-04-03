package com.example.alin.lechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ConversationActivity extends AppCompatActivity {

    private String PRIVATE_CONVERSATION_TITLE = "Private conversation with ";
    private String GROUP_CONVERSATION_TITLE = "Group conversation: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        String conversationType = intent.getStringExtra(HomeActivity.EXTRA_CONVERSATION_TYPE);

        if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_PRIVATE)){
            getSupportActionBar().setTitle(PRIVATE_CONVERSATION_TITLE + intent.getStringExtra(HomeActivity.EXTRA_FRIEND_NAME));
        } else if(conversationType.equals(HomeActivity.CONVERSATION_TYPE_GROUP)){
            getSupportActionBar().setTitle(GROUP_CONVERSATION_TITLE + intent.getStringExtra(HomeActivity.EXTRA_GROUP_NAME));
        }
    }
}
