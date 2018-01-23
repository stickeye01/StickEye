package com.example.taehyo.call;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
    }
    public void onClick(View v) {
        Intent intent=new Intent(this ,SubActivity1.class);
        startActivity(intent);
    }

    public void onClick1(View v) {
        Intent intent=new Intent(this ,SMS1.class);
        startActivity(intent);
    }
}
