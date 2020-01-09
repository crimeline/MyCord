package com.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.diagnosislibrary.DiagnosisManagement;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String websocketUrl = "wss://iepg-sy.vosnewland.com/ums";
        String uploadLogUrl = "http://shijihulian.in.3322.org:13847/ynm";
        DiagnosisManagement.getInstance().init(this,null, websocketUrl, uploadLogUrl, 5*1024*1024, 30, null);
        DiagnosisManagement.getInstance().startLog();
    }
}
