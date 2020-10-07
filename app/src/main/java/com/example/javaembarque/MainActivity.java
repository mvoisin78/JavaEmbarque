package com.example.javaembarque;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToServer(View view) {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

    public void goToVideo(View view) {
        Intent intent = new Intent(this, VideoPlayer.class);
        startActivity(intent);
    }

    public void goToB(View view) {
        Intent intent = new Intent(this, Bluetooth.class);
        startActivity(intent);
    }
}