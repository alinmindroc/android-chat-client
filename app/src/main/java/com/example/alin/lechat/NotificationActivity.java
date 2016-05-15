package com.example.alin.lechat;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import JSON_objects.JSONGroupMessage;
import JSON_objects.JSONMessage;
import JSON_objects.JSONNotification;

public class NotificationActivity extends AppCompatActivity {

    String currentUserId;

    final Handler handler = new Handler();
    Runnable runnableNotifications = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            new HttpRequestGetNotifications().execute(currentUserId);
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableNotifications, 2000);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(runnableNotifications);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableNotifications);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnableNotifications);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra(LoginActivity.EXTRA_CURRENT_USER_ID);
    }

    class NotificationAdapter extends ArrayAdapter<JSONNotification> {
        ArrayList<JSONNotification> notifications;
        private final int CRT_USER_MESSAGE_TYPE = 1;
        private final int OTHER_USER_MESSAGE_TYPE = 0;

        public NotificationAdapter(Context context, ArrayList<JSONNotification> notifications) {
            super(context, 0, notifications);
            this.notifications = notifications;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final JSONNotification notification = getItem(position);

            LayoutInflater lInflater = LayoutInflater.from(getContext());
            convertView = lInflater.inflate(R.layout.notification_list_entry, parent, false);

            TextView tvContent = (TextView) convertView.findViewById(R.id.notificationText);
            Button buttonDelete = (Button) convertView.findViewById(R.id.removeNotificationButton);

            tvContent.setText(notification.getContent());

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new HttpRequestRemoveNotification().execute(notification.getId());
                }
            });

            return convertView;
        }
    }

    private class HttpRequestRemoveNotification extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                final String url = Constants.serverUrl;

                String targetUrl = UriComponentsBuilder.fromUriString(url)
                        .path("/removeNotification")
                        .queryParam("notificationId", params[0])
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
    }

    private class HttpRequestGetNotifications extends AsyncTask<String, Void, List<LinkedHashMap>> {
        @Override
        protected List<LinkedHashMap> doInBackground(String... params) {
            try {
                final String url = Constants.serverUrl;

                String targetUrl = UriComponentsBuilder.fromUriString(url)
                        .path("/notification")
                        .queryParam("recipientId", params[0])
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
        protected void onPostExecute(List<LinkedHashMap> notifications) {
            ArrayList<JSONNotification> arrayOfNotifications = new ArrayList<>();

            for(LinkedHashMap m: notifications){
                JSONNotification notification = new JSONNotification();
                notification.setRecipientId(m.get("recipientId").toString());
                notification.setContent(m.get("content").toString());
                notification.setAccepted(m.get("accepted").toString());
                notification.setId(Integer.valueOf(m.get("id").toString()));

                arrayOfNotifications.add(notification);
            }

            final ArrayList<Message> arrayOfMessages = new ArrayList<Message>();
            final ListView notificationList = (ListView) findViewById(R.id.notificationListView);
            final NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationActivity.this, arrayOfNotifications);

            notificationList.setAdapter(notificationAdapter);

            notificationAdapter.notifyDataSetChanged();
        }
    }

}
