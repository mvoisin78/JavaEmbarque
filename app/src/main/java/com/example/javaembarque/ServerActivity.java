package com.example.javaembarque;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btn_download;
    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);


       /* String toastText;
        if(bluetooth.isEnabled()){
            String address=bluetooth.getAddress();
            String name=bluetooth.getName();
            toastText=name+" : "+address;
        }
        else
            toastText="Bluetooth is not enabled";
        Toast.makeText(this, toastText,Toast.LENGTH_LONG).show();*/

        btn_download = (Button) findViewById(R.id.download_btn);
        videoView=(VideoView)findViewById(R.id.videoView);
        Uri vidurl=Uri.parse("https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4");
        videoView.setVideoURI(vidurl);
        videoView.start();
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                download();

            }
        });
    }

    private void download() {
        Downback DB = new Downback();
        DB.execute("");

    }


    private class Downback extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            final String vidurl = "https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";
            downloadfile(vidurl);
            return null;

        }


    }

    private void downloadfile(String vidurl) {

        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String name = "video" + date + ".mp4";

        try {
            String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + File.separator + "My_Video";
            System.out.println("it's ok");
            File rootFile = new File(rootDir);
            rootFile.mkdir();
            URL url = new URL(vidurl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(rootFile,
                    name));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
        } catch (IOException e) {
            Log.d("Error....", e.toString());
        }
    }
}