package com.example.alin.lechat;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

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



    }

    class User {
        public String name;

        public User(String name) {
            this.name = name;
        }
    }

    class UsersAdapter extends ArrayAdapter<User> {
        public UsersAdapter(Context context, ArrayList<User> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            User user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_list_entry, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.nameLabel);
            // Populate the data into the template view using the data object
            tvName.setText(user.name);
            // Return the completed view to render on screen
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
        public GroupsAdapter(Context context, ArrayList<Group> groups) {
            super(context, 0, groups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Group group = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_list_entry, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.nameLabel);
            // Populate the data into the template view using the data object
            tvName.setText(group.name);
            // Return the completed view to render on screen
            return convertView;
        }
    }

}
