package com.tizianobasile.wearsdkdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class WearActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    //DATA LAYER
    private GoogleApiClient mGoogleApiClient;

    //UI
    private TextView mDataItemSyncMessageTextView;
    private ImageView mAssetImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        //Register GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mDataItemSyncMessageTextView = (TextView) stub.findViewById(R.id.dataItemMsgTV);
                mAssetImageView = (ImageView) stub.findViewById(R.id.assetImageView);
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
        Log.d("WEAR", "GoogleApiClient connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("WEAR", "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("WEAR", "GoogleApiClient connection failed");
    }

    //DATA LAYER LISTENERS

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for(DataEvent mDataEvent : dataEvents){
            if(mDataEvent.getType() == DataEvent.TYPE_DELETED){
                Log.d("WEAR", "DataItem Deleted: " + mDataEvent.getDataItem().getUri());
            }
            else if(mDataEvent.getType() == DataEvent.TYPE_CHANGED){
                DataItem mDataItem = mDataEvent.getDataItem();
                DataMapItem mDataMapItem = DataMapItem.fromDataItem(mDataItem);
                final String mDataItemMessage = mDataMapItem.getDataMap().getString("DATA_MESSAGE");
                Asset mAsset = mDataMapItem.getDataMap().getAsset("ASSET_BITMAP");
                final Bitmap mAssetBitmap = loadBitmapFromAsset(mAsset);
                Log.d("WEAR", mDataItemMessage);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDataItemSyncMessageTextView.setVisibility(View.VISIBLE);
                            mDataItemSyncMessageTextView.setText(mDataItemMessage);
                            if(mAssetBitmap != null){
                                mDataItemSyncMessageTextView.setVisibility(View.GONE);
                                mAssetImageView.setImageBitmap(mAssetBitmap);
                                mAssetImageView.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), mDataItemMessage, Toast.LENGTH_LONG).show();
                            }else{
                                mAssetImageView.setVisibility(View.GONE);
                            }
                        }
                    });

            }
        }
    }

    //UTILS
    public Bitmap loadBitmapFromAsset(Asset mAsset){
        if (mAsset == null){
            throw new IllegalArgumentException("Asset must be not null");
        }
        ConnectionResult mConnectionResult = mGoogleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
        if(!mConnectionResult.isSuccess()){
            return null;
        }

        InputStream mAssetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, mAsset).await().getInputStream();
        mGoogleApiClient.disconnect();
        if(mAssetInputStream == null){
            return null;
        }
        return BitmapFactory.decodeStream(mAssetInputStream);
    }
}
