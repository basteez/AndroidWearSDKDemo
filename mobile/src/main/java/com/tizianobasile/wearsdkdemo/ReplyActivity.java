package com.tizianobasile.wearsdkdemo;

import android.support.v4.app.RemoteInput;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class ReplyActivity extends ActionBarActivity {

    private String voiceReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voiceReply = getVoiceReply();
        if(voiceReply.toLowerCase().equals("yes")){
            setContentView(R.layout.activity_reply_yes);
        }else if(voiceReply.toLowerCase().equals("no")){
            setContentView(R.layout.activity_reply_no);
        }else{
            setContentView(R.layout.activity_reply_oops);
        }


    }

    public String getVoiceReply(){
        Intent intent = getIntent();
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        String reply = "";
        if(remoteInput != null){
            reply = remoteInput.getCharSequence(HandheldMainActivity.EXTRA_VOICE_REPLY).toString();
        }
        return reply;
    }


}
