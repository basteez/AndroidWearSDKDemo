package com.tizianobasile.wearsdkdemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by tiziano on 22/10/14.
 */
public class DataItemSyncActivity extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    //DATA LAYER
    private GoogleApiClient mGoogleApiClient;

    //UI
    private EditText mDataItemMessageEditText;
    private Button mSyncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_item_sync);
        //Register GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        //Get UI Elements references
        mDataItemMessageEditText = (EditText) findViewById(R.id.messageET);
        mSyncButton = (Button) findViewById(R.id.syncBtn);
        mSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dataItemMessage = mDataItemMessageEditText.getText().toString();
                //TODO Create Asset
                //...
                PutDataMapRequest mDataMapRequest = PutDataMapRequest.create("/data");
                mDataMapRequest.getDataMap().putString("DATA_MESSAGE", dataItemMessage);
                PutDataRequest mDataRequest = mDataMapRequest.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> mPendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, mDataRequest);
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

    //INTERFACES IMPLEMENTATION

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
