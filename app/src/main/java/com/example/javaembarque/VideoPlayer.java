package com.example.javaembarque;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import androidx.annotation.NonNull;

public class VideoPlayer extends Activity {

    private VideoView vidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Get a reference to the VideoView instance as follows, using the id we set in the XML layout.
        vidView = (VideoView)findViewById(R.id.video);

        // Add playback controls.
        MediaController vidControl = new MediaController(this);
        // Set it to use the VideoView instance as its anchor.
        vidControl.setAnchorView(vidView);
        // Set it as the media controller for the VideoView object.
        vidView.setMediaController(vidControl);

        // Prepare the URI for the endpoint.
        String vidAddress = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "My_Video/video.mp4";
        Uri vidUri = Uri.parse(vidAddress);
        // Parse the address string as a URI so that we can pass it to the VideoView object.
        vidView.setVideoURI(vidUri);
        // Start playback.
        vidView.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("time",vidView.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        vidView.seekTo(savedInstanceState.getInt("time"));
    }
}