package com.example.alin.lechat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private final String ADD_FRIEND_LIST_ENTRY = "+ Add Friend";
    private final String ADD_GROUP_LIST_ENTRY = "+ Add Group";
    private final String ADD_COLOR_HEX = "#2C90D8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ListView friendList = (ListView) findViewById(R.id.friendList);
        ListView groupList = (ListView) findViewById(R.id.groupList);

        //add users

        ArrayList<User> arrayOfUsers = new ArrayList<User>();
        // Create the adapter to convert the array to views

        arrayOfUsers.add(new User("Alin"));
        arrayOfUsers.add(new User("Adriana"));
        arrayOfUsers.add(new User("Gica"));
        arrayOfUsers.add(new User("Laura"));
        arrayOfUsers.add(new User("Cristi"));

        UsersAdapter friendsAdapter = new UsersAdapter(this, arrayOfUsers);
        // Attach the adapter to a ListView
        friendList.setAdapter(friendsAdapter);


        //add groups
        ArrayList<Group> arrayOfGroups = new ArrayList<Group>();
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
        arrayOfUsers.add(new User(ADD_FRIEND_LIST_ENTRY));
        arrayOfGroups.add(new Group(ADD_GROUP_LIST_ENTRY));


        //add logic for opening conversations
    }

    class User {
        public String name;

        public User(String name) {
            this.name = name;
        }
    }

    class UsersAdapter extends ArrayAdapter<User> {
        int dataSize;
        private final int LAST_POSITION_VIEW_TYPE = 1;
        private final int NON_LAST_POSITION_VIEW_TYPE = 0;

        public UsersAdapter(Context context, ArrayList<User> users) {
            super(context, 0, users);
            this.dataSize = users.size();
        }

        @Override
        public int getItemViewType(int position) {
            // Define a way to determine which layout to use, here it's just evens and odds.
            return position == dataSize ? LAST_POSITION_VIEW_TYPE : NON_LAST_POSITION_VIEW_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Count of different layouts
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            User user = getItem(position);

            LayoutInflater lInflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == LAST_POSITION_VIEW_TYPE) {
                convertView = lInflater.inflate(R.layout.add_list_entry, parent, false);
            } else {
                convertView = lInflater.inflate(R.layout.friend_list_entry, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.nameLabel);
            tvName.setText(user.name);

            if (getItemViewType(position) == LAST_POSITION_VIEW_TYPE) {
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
        private final int LAST_POSITION_VIEW_TYPE = 1;
        private final int NON_LAST_POSITION_VIEW_TYPE = 0;

        public GroupsAdapter(Context context, ArrayList<Group> groups) {
            super(context, 0, groups);
            this.dataSize = groups.size();
        }

        @Override
        public int getItemViewType(int position) {
            // Define a way to determine which layout to use, here it's just evens and odds.
            return position == dataSize ? LAST_POSITION_VIEW_TYPE : NON_LAST_POSITION_VIEW_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Count of different layouts
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = getItem(position);

            LayoutInflater lInflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == LAST_POSITION_VIEW_TYPE) {
                convertView = lInflater.inflate(R.layout.add_list_entry, parent, false);
            } else {
                convertView = lInflater.inflate(R.layout.group_list_entry, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.nameLabel);
            tvName.setText(group.name);

            if (getItemViewType(position) == LAST_POSITION_VIEW_TYPE) {
                tvName.setTextColor(Color.parseColor(ADD_COLOR_HEX));
            }

            return convertView;
        }
    }
}
