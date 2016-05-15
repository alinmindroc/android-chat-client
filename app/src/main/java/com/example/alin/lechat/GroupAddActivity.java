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

import java.security.acl.Group;

import JSON_objects.JSONGroup;

public class GroupAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);

        Intent intent = getIntent();
        final String currentUserId = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID);

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
                                    findViewById(R.id.membersAutoComplete);

                            mt.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

                            ArrayAdapter<String> adp = new ArrayAdapter<String>(GroupAddActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, friendsNames);
                            mt.setInputType(InputType.TYPE_CLASS_TEXT);

                            mt.setThreshold(1);
                            mt.setAdapter(adp);

                            //add listener for group add button
                            Button addGroupButton = (Button) findViewById(R.id.addGroupButton);
                            addGroupButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    JSONGroup jsonGroup = new JSONGroup();

                                    String groupName = ((EditText) findViewById(R.id.groupNameEditText)).getText().toString();
                                    if(groupName.length() == 0){
                                        Toast.makeText(GroupAddActivity.this, "Please set a group name", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    jsonGroup.setName(groupName);
                                    String memberNamesString = ((EditText) findViewById(R.id.membersAutoComplete)).getText().toString();

                                    //add the current user in every group he creates
                                    String memberIdsString = currentUserId + ":";
                                    String[] memberNames = memberNamesString.split(",");
                                    for(String s: memberNames){
                                        for(int i=0; i<facebookFriends.length(); i++) {
                                            try {
                                                if(facebookFriends.getJSONObject(i).getString("name").equals(s.trim())){
                                                    memberIdsString = memberIdsString + (facebookFriends.getJSONObject(i).getString("id") + ":");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    jsonGroup.setMembersId(memberIdsString);

                                    new HttpRequestAddGroup().execute(jsonGroup);
                                    GroupAddActivity.this.finish();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
    }

    private class HttpRequestAddGroup extends AsyncTask<JSONGroup, Void, String> {
        @Override
        protected String doInBackground(JSONGroup... params) {
            try {
                final String url = "http://188.247.227.127:8080/group";
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
}
