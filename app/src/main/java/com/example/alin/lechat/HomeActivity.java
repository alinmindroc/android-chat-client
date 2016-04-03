package com.example.alin.lechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private final String ADD_FRIEND_LIST_ENTRY = "+ Add Friend";
    private final String ADD_GROUP_LIST_ENTRY = "+ Add Group";
    private final String ADD_COLOR_HEX = "#2C90D8";

    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_GROUP_NAME = "group_name";
    public static final String EXTRA_CONVERSATION_TYPE = "conversation_type";
    public static final String CONVERSATION_TYPE_PRIVATE = "private_conversation";
    public static final String CONVERSATION_TYPE_GROUP = "group_conversation";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        ListView friendList = (ListView) findViewById(R.id.friendList);
        ListView groupList = (ListView) findViewById(R.id.groupList);

        //add users

        final ArrayList<User> arrayOfUsers = new ArrayList<User>();
        // Create the adapter to convert the array to views

        arrayOfUsers.add(new User("Andrei"));
        arrayOfUsers.add(new User("Adriana"));
        arrayOfUsers.add(new User("Gica"));
        arrayOfUsers.add(new User("Laura"));
        arrayOfUsers.add(new User("Cristi"));

        UsersAdapter friendsAdapter = new UsersAdapter(this, arrayOfUsers);
        // Attach the adapter to a ListView
        friendList.setAdapter(friendsAdapter);


        //add groups
        final ArrayList<Group> arrayOfGroups = new ArrayList<Group>();
        // Create the adapter to convert the array to views

        arrayOfGroups.add(new Group("Group 1"));
        arrayOfGroups.add(new Group("Group 2"));
        arrayOfGroups.add(new Group("Android"));
        arrayOfGroups.add(new Group("Facebook"));
        arrayOfGroups.add(new Group("Facultate"));
        arrayOfGroups.add(new Group("Idp"));

        GroupsAdapter groupsAdapter = new GroupsAdapter(this, arrayOfGroups);
        // Attach the adapter to a ListView
        groupList.setAdapter(groupsAdapter);


        //add groups and friends
        arrayOfUsers.add(0, new User(ADD_FRIEND_LIST_ENTRY));
        arrayOfGroups.add(0, new Group(ADD_GROUP_LIST_ENTRY));


        //add logic for opening conversations
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    //start add friend activity
                    Intent intent = new Intent(view.getContext(), FriendAddActivity.class);
                    startActivity(intent);
                } else {
                    //go to friend conversation
                    Intent intent = new Intent(view.getContext(), ConversationActivity.class);

                    intent.putExtra(EXTRA_CONVERSATION_TYPE, CONVERSATION_TYPE_PRIVATE);
                    intent.putExtra(EXTRA_FRIEND_NAME, arrayOfUsers.get(position).name);
                    startActivity(intent);
                }
            }
        });

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    //start add group activity
                    Intent intent = new Intent(view.getContext(), GroupAddActivity.class);
                    startActivity(intent);
                } else {
                    //go to group conversation
                    Intent intent = new Intent(view.getContext(), ConversationActivity.class);

                    intent.putExtra(EXTRA_CONVERSATION_TYPE, CONVERSATION_TYPE_GROUP);
                    intent.putExtra(EXTRA_GROUP_NAME, arrayOfGroups.get(position).name);
                    startActivity(intent);
                }
            }
        });
    }

    class User {
        public String name;

        public User(String name) {
            this.name = name;
        }
    }

    class UsersAdapter extends ArrayAdapter<User> {
        int dataSize;
        private final int FIRST_POSITION_VIEW_TYPE = 1;
        private final int NON_FIRST_POSITION_VIEW_TYPE = 0;

        public UsersAdapter(Context context, ArrayList<User> users) {
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
            User user = getItem(position);

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

        public Group(String name) {
            this.name = name;
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
}
