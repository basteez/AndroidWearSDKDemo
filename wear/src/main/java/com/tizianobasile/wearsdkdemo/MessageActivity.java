package com.tizianobasile.wearsdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;

/**
 * Created by tiziano on 23/10/14.
 */
public class MessageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

            }
        });
    }
}
