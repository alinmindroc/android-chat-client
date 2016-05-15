package com.example.alin.lechat;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import JSON_objects.JSONGroup;
import JSON_objects.JSONNotification;

public class AddFriendsToGroupActivity extends AppCompatActivity {

    String currentUserId;
    String currentUserName;
    String currentGroupId;
    String currentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_to_group);

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID);
        currentUserName = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_NAME);
        currentGroupId = intent.getStringExtra(HomeActivity.EXTRA_GROUP_ID);
        currentGroupName = intent.getStringExtra(HomeActivity.EXTRA_GROUP_NAME);

        GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            final JSONArray facebookFriends = response.getJSONObject().getJSONArray("data");
                            String[] friendsNames = new String[facebookFriends.length()];

                            for(int i=0; i<facebookFriends.length(); i++) {
                                friendsNames[i] = facebookFriends.getJSONObject(i).getString("name");
                            }

                            //set in the friend input the list of users for autocomplete
                            MultiAutoCompleteTextView mt=(MultiAutoCompleteTextView)
                                    findViewById(R.id.friendsAutoComplete);

                            mt.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

                            ArrayAdapter<String> adp = new ArrayAdapter<String>(AddFriendsToGroupActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, friendsNames);
                            mt.setInputType(InputType.TYPE_CLASS_TEXT);

                            mt.setThreshold(1);
                            mt.setAdapter(adp);

                            //add listener for group add button
                            Button addGroupButton = (Button) findViewById(R.id.addFriendsButton);
                            addGroupButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String friendsNamesString = ((EditText) findViewById(R.id.friendsAutoComplete)).getText().toString();

                                    //add the current user in every group he creates
                                    String friendsIdsString = currentUserId + ":";
                                    String[] friendsNames = friendsNamesString.split(",");

                                    for(String s: friendsNames){
                                        for(int i=0; i<facebookFriends.length(); i++) {
                                            try {
                                                if(facebookFriends.getJSONObject(i).getString("name").equals(s.trim())){
                                                    friendsIdsString = friendsIdsString + (facebookFriends.getJSONObject(i).getString("id") + ":");
                                                    sendGroupInvitation(facebookFriends.getJSONObject(i).getString("id"), currentGroupName);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    new HttpRequestAddFriendsToGroup().execute(currentGroupId, friendsIdsString);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();

    }

    private class HttpRequestAddFriendsToGroup extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                final String url = "http://188.247.227.127:8080";

                String targetUrl= UriComponentsBuilder.fromUriString(url)
                        .path("/addFriendsToGroup")
                        .queryParam("groupId", Integer.valueOf(params[0]))
                        .queryParam("friendsIds", params[1])
                        .build()
                        .toUri()
                        .toString();

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return (String) restTemplate.getForObject(targetUrl, String.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String messages) {
            if(messages == null){
                return;
            }
            Log.e("asd", messages.toString());
        }
    }

    private void sendGroupInvitation(String recipientId, String groupName){
        JSONNotification jsonNotification = new JSONNotification();

        jsonNotification.setAccepted("false");
        jsonNotification.setContent(currentUserName + " has invited you to group " + groupName);
        jsonNotification.setRecipientId(recipientId);

        new HttpRequestAddNotification().execute(jsonNotification);
    }

    private class HttpRequestAddNotification extends AsyncTask<JSONNotification, Void, String> {
        @Override
        protected String doInBackground(JSONNotification... params) {
            try {
                final String url = "http://188.247.227.127:8080/notification";
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
            AddFriendsToGroupActivity.this.finish();
        }
    }
}
