package com.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.diagnosislibrary.DiagnosisManagement;
import com.android.diagnosislibrary.utils.Logger.Logger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String tag = DiagnosisManagement.getInstance(this).getTAG();
        Logger.d(TAG, "onCreate: tag --> " + tag);
//        DiagnosisManagement.getInstance(this).cmdrunning();
        DiagnosisManagement.getInstance(this).start();
        Logger.d(TAG, "onCreate: tag --> " + tag);
    }
}
