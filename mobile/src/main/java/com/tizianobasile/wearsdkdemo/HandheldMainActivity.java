package com.tizianobasile.wearsdkdemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;


public class HandheldMainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //NOTIFICATION IDs CONSTANTS
    public static final int SIMPLE_NOTIFICATION_ID = 11;
    public static final int STANDARD_ACTION_NOTIFICATION_ID = 22;
    public static final int CUSTOM_ACTOIN_NOTIFICATION_ID = 33;
    public static final int VOICE_INPUT_NOTIFICATION_ID = 44;
    public static final int PAGED_NOTIFICATION_ID = 55;
    public static final int GROUPED_NOTIFICATION_ID = 66;

    //EXTRAS CONSTANTS
    public static final String EXTRA_VOICE_REPLY = "VoiceReply";

    //GROUP CONSTANTS
    public static final String GROUP_ID = "com.tizianobasile.wearsdkdemo.group";

    //GUI
    private ListView mMenuList;
    private String[] mMenuEntries;

    //DATA LAYER
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handheld_main);
        //Build GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        //Get Layout References
        mMenuList = (ListView) findViewById(R.id.menuList);
        //Populate menu
        mMenuEntries = getResources().getStringArray(R.array.menu_main);
        mMenuList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mMenuEntries));
        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
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
                    case 6:
                        //Sync DataItem with asset
                        Intent mDataItemSyncIntent = new Intent(getApplicationContext(), DataItemSyncActivity.class);
                        startActivity(mDataItemSyncIntent);
                        break;
                    case 7:
                        new MessageTask().execute();
                        //sendMessageToWear();
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //NOTIFICATION METHODS

    public Notification createSimpleNotification(){
        Notification mNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Simple Notification")
                .setContentText("Just a title, a text and an icon")
                .build();
        return mNotification;
    }

    public Notification createStandardActionNotification(){
        Intent mIntent = new Intent(this, NotificationIntentActivity.class);
        mIntent.putExtra("EventID", 1);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);

        Notification mNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Standard Action Notification")
                .setContentText("Swipe Left to show the action")
                .setContentIntent(mPendingIntent)
                .build();
        return mNotification;
    }

    public Notification createCustomActionNotification(){
        Intent mIntent = new Intent(Intent.ACTION_VIEW);
        Uri mPosition = Uri.parse("geo:0,0?q=41.109388,16.878843");
        mIntent.setData(mPosition);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);

        Notification mNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Custom Action Notification")
                .setContentText("Swipe Left to check the action")
                .addAction(R.drawable.ic_location, "Check your position", mPendingIntent)
                .build();
        return mNotification;
    }

    public Notification createVoiceInputNotification(){
        String[] mChoices = {"Yes", "No"};

        Intent mIntent = new Intent(this, ReplyActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput mRemoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("Are you happy?")
                .setChoices(mChoices)
                .build();

        Action mAction = new Action.Builder(R.drawable.ic_reply, "Reply", mPendingIntent)
                .addRemoteInput(mRemoteInput)
                .build();

        WearableExtender mExtender = new WearableExtender()
                .addAction(mAction);

        Notification mNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("VoiceInput Notification")
                .setContentText("Swipe Left to see the voice action")
                .extend(mExtender)
                .build();
        return mNotification;
    }

    public Notification createPagedNotification(){

        NotificationCompat.Builder mFirstPageBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Paged notification 1/2")
            .setContentText("This is the first page");

        Notification mSecondPage =
                new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Paged Notification 2/2")
            .setContentText("This is the second page")
        .build();

        Notification mNotification =
                new WearableExtender()
                .addPage(mSecondPage)
                .extend(mFirstPageBuilder)
                .build();
        return mNotification;
    }

    public Notification[] createGroupedNotification(){
        Notification mFirstNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("First Notification")
                .setContentText("Hi, I'm a notification")
                .setGroup(GROUP_ID)
                .build();

        Notification mSecondNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Second Notification")
                .setContentText("Yay, here's the second one!")
                .setGroup(GROUP_ID)
                .build();

        Bitmap mLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        Notification mSummaryNotification = new NotificationCompat.Builder(this)
                .setContentTitle("2 Notifications fromw WearSDKDemo")
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(mLargeIcon)
                .setStyle(new NotificationCompat.InboxStyle()
                    .addLine("First Notification    Hi, I'm a notification")
                    .addLine("Second Notification   Yay, here's the second one!")
                    .setBigContentTitle("2 Notifications from WearSDKDemo")
                    .setSummaryText("com.tizianobasile.wearsdkdemo"))
                .setGroup(GROUP_ID)
                .setGroupSummary(true)
                .build();

        Notification[] mNotifications = new Notification[]{mFirstNotification, mSecondNotification, mSummaryNotification};

        return mNotifications;
    }

    public void issueNotification(int mNotificationId, Notification mNotification){
        //Get a reference for NotificationManagerCompat
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        //issue the notification
        mNotificationManager.notify(mNotificationId, mNotification);
    }

    public void issueNotification(int notificationId, Notification[] mNotification){
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        int i = 0;
        for(Notification notification : mNotification){
            mNotificationManager.notify(notificationId + i, notification);
            i++;
        }
    }

    //MESSAGE API METHODS
    public void sendMessageToWear(){
        Log.d("MOBILE", "sendMessageToWear");
        Collection<String> mNodes = getNodes();
        Log.d("MOBILE", mNodes.toString());
        for(String node : mNodes){
                /*MessageApi.SendMessageResult mSendMessageResult = */Wearable.MessageApi.sendMessage(mGoogleApiClient, node, "/start/MessageActivity", null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d("MOBILE", sendMessageResult.getStatus().toString());
                        if(!sendMessageResult.getStatus().isSuccess()){
                            Log.e("MOBILE", "Error sending message to wear: " + sendMessageResult.getStatus());
                        }
                    }
                });
        }
    }

    private Collection<String> getNodes(){
        final HashSet<String> mResults = new HashSet<String>();
        /*NodeApi.GetConnectedNodesResult mNodes =*/ Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for(Node mNode : getConnectedNodesResult.getNodes()){
                    mResults.add(mNode.getId());
                }
            }
        });
        /*for(Node mNode : mNodes.getNodes()){
            mResults.add(mNode.getId());
        }*/

        return mResults;
    }

    //ASYNCTAKS FOR MESSANGE SENDING
    private class MessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            sendMessageToWear();
            return null;
        }
    }

    //DATA LAYER CALLBACKS

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("MOBILE", "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MOBILE", "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("MOBILE", "GoogleApiClient connection failed");
    }
}
