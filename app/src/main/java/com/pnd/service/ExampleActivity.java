package com.pnd.service;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.pnd.service.services.PandoraService;

public class ExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PandoraService pandora = new PandoraService();
        pandora.setAuthListener(new PandoraService.PandoraListener() {

            @Override
            public void onSuccced(String user_id) {
                Log.d("TAG","Login succed");
            }

            @Override
            public void onFailed(String msg) {
                Log.d("TAG","Login failed");
            }

            @Override
            public void onCancelled(String msg) {
                Log.d("TAG","Login cancelled");
            }
        }, ExampleActivity.this);

        findViewById(R.id.login).setOnClickListener(view -> pandora.auth());

    }

}

