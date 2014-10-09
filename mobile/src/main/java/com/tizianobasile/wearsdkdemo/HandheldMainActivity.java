package com.tizianobasile.wearsdkdemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class HandheldMainActivity extends ActionBarActivity {
    //NOTIFICATION IDs CONSTANTS
    public static final int SIMPLE_NOTIFICATION_ID = 11;
    public static final int STANDARD_ACTION_NOTIFICATION_ID = 22;
    public static final int CUSTOM_ACTOIN_NOTIFICATION_ID = 33;
    public static final int VOICE_INPUT_NOTIFICATION_ID = 44;
    public static final int PAGED_NOTIFICATION_ID = 55;
    public static final int GROUPED_NOTIFICATION_ID = 66;

    //EXTRAS CONSTANTS
    public static final String EXTRA_VOICE_REPLY = "VoiceReply";

    //GUI
    private ListView menuList;

    private String[] menuEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handheld_main);
        //Get Layout References
        menuList = (ListView) findViewById(R.id.menuList);
        //Populate menu
        menuEntries = getResources().getStringArray(R.array.menu_main);
        menuList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuEntries));
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        //Simple Notification
                        issueNotification(SIMPLE_NOTIFICATION_ID, createSimpleNotification());
                        break;
                    case 1:
                        //Standard Action Notification
                        issueNotification(STANDARD_ACTION_NOTIFICATION_ID, createStandardActionNotification());
                        break;
                    case 2:
                        //Custom Action Notification
                        issueNotification(CUSTOM_ACTOIN_NOTIFICATION_ID, createCustomActionNotification());
                        break;
                    case 3:
                        //Voice Input Notification
                        issueNotification(VOICE_INPUT_NOTIFICATION_ID, createVoiceInputNotification());
                        break;
                    case 4:
                        //Paged Notification
                        issueNotification(PAGED_NOTIFICATION_ID, createPagedNotification());
                        break;
                    case 5:
                        //Grouped Notification
                        issueNotification(GROUPED_NOTIFICATION_ID, createGroupedNotification());
                        break;
                }
            }
        });
    }

    //NOTIFICATION METHODS

    public Notification createSimpleNotification(){
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Simple Notification")
                .setContentText("Just a title, a text and an icon")
                .build();
        return notification;
    }

    public Notification createStandardActionNotification(){
        Intent intent = new Intent(this, NotificationIntentActivity.class);
        intent.putExtra("EventID", 1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Standard Action Notification")
                .setContentText("Swipe Left to show the action")
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

    public Notification createCustomActionNotification(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri position = Uri.parse("geo:0,0?q=41.109388,16.878843");
        intent.setData(position);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Custom Action Notification")
                .setContentText("Swipe Left to check the action")
                .addAction(R.drawable.ic_location, "Check your position", pendingIntent)
                .build();
        return notification;
    }

    public Notification createVoiceInputNotification(){
        String[] choices = {"Yes", "No"};

        Intent intent = new Intent(this, ReplyActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("Are you happy?")
                .setChoices(choices)
                .build();

        Action action = new Action.Builder(R.drawable.ic_reply, "Reply", pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        WearableExtender extender = new WearableExtender()
                .addAction(action);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("VoiceInput Notification")
                .setContentText("Swipe Left to see the voice action")
                .extend(extender)
                .build();
        return notification;
    }

    public Notification createPagedNotification(){

        return null;
    }

    public Notification createGroupedNotification(){return null;}

    public void issueNotification(int mNotificationId, Notification mNotification){
        //Get a reference for NotificationManagerCompat
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        //issue the notification
        mNotificationManager.notify(mNotificationId, mNotification);
    }

}
