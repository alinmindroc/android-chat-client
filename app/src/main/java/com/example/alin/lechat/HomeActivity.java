package com.example.alin.lechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private final String ADD_FRIEND_LIST_ENTRY = "+ Add Friend";
    private final String ADD_GROUP_LIST_ENTRY = "+ Add Group";
    private final String ADD_COLOR_HEX = "#2C90D8";

    public static final String EXTRA_FRIEND_ID = "friend_ID";
    public static final String EXTRA_FRIEND_NAME = "friend_name";

    public static final String EXTRA_CONVERSATION_TYPE = "conversation_type";
    public static final String CONVERSATION_TYPE_PRIVATE = "private_conversation";
    public static final String CONVERSATION_TYPE_GROUP = "group_conversation";

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_NAME = "group_name";

    String currentUserId;
    String currentUserName;

    final Handler handler = new Handler();
    Runnable runnableUpdateGroups = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            new HttpRequestGetGroups().execute(getIntent().getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID));
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableUpdateGroups, 2000);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(runnableUpdateGroups);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnableUpdateGroups);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableUpdateGroups);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final ListView friendList = (ListView) findViewById(R.id.friendList);

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID);
        currentUserName = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_NAME);

        //set friends list
        GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            final JSONArray friends = response.getJSONObject().getJSONArray("data");

                            final ArrayList<FacebookUser> arrayOfUsers = new ArrayList();
                            // Create the adapter to convert the array to views

                            UsersAdapter friendsAdapter = new UsersAdapter(HomeActivity.this, arrayOfUsers);
                            // Attach the adapter to a ListView
                            friendList.setAdapter(friendsAdapter);
                            arrayOfUsers.add(0, new FacebookUser(ADD_FRIEND_LIST_ENTRY));

                            for(int i=0; i<friends.length(); i++) {
                                arrayOfUsers.add(new FacebookUser(friends.getJSONObject(i).getString("name")));
                            }

                            //add logic for opening conversations
                            friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (position == 0) {
                                        //start add friend activity
                                        Intent intent = new Intent(view.getContext(), FriendAddActivity.class);
                                        startActivity(intent);
                                    } else {
                                        //go to friend conversation
                                        Intent intent = new Intent(view.getContext(), ConversationActivity.class);

                                        intent.putExtra(EXTRA_CONVERSATION_TYPE, CONVERSATION_TYPE_PRIVATE);
                                        try {
                                            intent.putExtra(EXTRA_FRIEND_ID, friends.getJSONObject(position - 1).getString("id"));
                                            intent.putExtra(EXTRA_FRIEND_NAME, friends.getJSONObject(position - 1).getString("name"));
                                            intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_NAME, currentUserName);
                                            intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_ID, currentUserId);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        startActivity(intent);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
    }

    class UsersAdapter extends ArrayAdapter<FacebookUser> {
        int dataSize;
        private final int FIRST_POSITION_VIEW_TYPE = 1;
        private final int NON_FIRST_POSITION_VIEW_TYPE = 0;

        public UsersAdapter(Context context, ArrayList<FacebookUser> users) {
            super(context, 0, users);
            this.dataSize = users.size();
        }

        @Override
        public int getItemViewType(int position) {
            // Define a way to determine which layout to use, here it's just evens and odds.
            return position == 0 ? FIRST_POSITION_VIEW_TYPE : NON_FIRST_POSITION_VIEW_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Count of different layouts
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FacebookUser user = getItem(position);

            LayoutInflater lInflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == FIRST_POSITION_VIEW_TYPE) {
                convertView = lInflater.inflate(R.layout.add_list_entry, parent, false);
            } else {
                convertView = lInflater.inflate(R.layout.friend_list_entry, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.nameLabel);
            tvName.setText(user.name);

            if (getItemViewType(position) == FIRST_POSITION_VIEW_TYPE) {
                tvName.setTextColor(Color.parseColor(ADD_COLOR_HEX));
            }

            return convertView;
        }
    }

    class Group {
        public String name;
        public String id;

        public Group(String id, String name) {
            this.name = name;
            this.id = id;
        }
    }

    class GroupsAdapter extends ArrayAdapter<Group> {
        int dataSize;
        private final int FIRST_POSITION_VIEW_TYPE = 1;
        private final int NON_FIRST_POSITION_VIEW_TYPE = 0;

        public GroupsAdapter(Context context, ArrayList<Group> groups) {
            super(context, 0, groups);
            this.dataSize = groups.size();
        }

        @Override
        public int getItemViewType(int position) {
            // Define a way to determine which layout to use, here it's just evens and odds.
            return position == 0 ? FIRST_POSITION_VIEW_TYPE : NON_FIRST_POSITION_VIEW_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Count of different layouts
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = getItem(position);

            LayoutInflater lInflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == FIRST_POSITION_VIEW_TYPE) {
                convertView = lInflater.inflate(R.layout.add_list_entry, parent, false);
            } else {
                convertView = lInflater.inflate(R.layout.group_list_entry, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.nameLabel);
            tvName.setText(group.name);

            if (getItemViewType(position) == FIRST_POSITION_VIEW_TYPE) {
                tvName.setTextColor(Color.parseColor(ADD_COLOR_HEX));
            }

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notifications) {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_ID, currentUserId);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class HttpRequestGetGroups extends AsyncTask<String, Void, List<LinkedHashMap>> {
        @Override
        protected List<LinkedHashMap> doInBackground(String... params) {
            try {
                final String url = "http://188.247.227.127:8080";

                String targetUrl= UriComponentsBuilder.fromUriString(url)
                        .path("/group")
                        .queryParam("memberId", params[0])
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
        protected void onPostExecute(List<LinkedHashMap> groups) {
            if(groups == null){
                return;
            }
            Log.e("asd", groups.toString());

            ListView groupList = (ListView) findViewById(R.id.groupList);

            final ArrayList<Group> arrayOfGroups = new ArrayList<Group>();
            // Create the adapter to convert the array to views

            for(LinkedHashMap g : groups){
                arrayOfGroups.add(new Group(g.get("id").toString(), g.get("name").toString()));
            }

            GroupsAdapter groupsAdapter = new GroupsAdapter(HomeActivity.this, arrayOfGroups);
            // Attach the adapter to a ListView
            groupList.setAdapter(groupsAdapter);

            //add groups and friends
            arrayOfGroups.add(0, new Group("-1", ADD_GROUP_LIST_ENTRY));

            groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        //start add group activity
                        Intent intent = new Intent(view.getContext(), GroupAddActivity.class);
                        intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_ID, currentUserId);
                        intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_NAME, currentUserName);

                        startActivity(intent);
                    } else {
                        //go to group conversation
                        Intent intent = new Intent(view.getContext(), ConversationActivity.class);

                        intent.putExtra(EXTRA_CONVERSATION_TYPE, CONVERSATION_TYPE_GROUP);
                        intent.putExtra(EXTRA_GROUP_NAME, arrayOfGroups.get(position).name);
                        intent.putExtra(EXTRA_GROUP_ID, arrayOfGroups.get(position).id);
                        intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_NAME, currentUserName);
                        intent.putExtra(LoginActivity.EXTRA_CURRENT_USER_ID, currentUserId);

                        startActivity(intent);
                    }
                }
            });
        }
    }
}
