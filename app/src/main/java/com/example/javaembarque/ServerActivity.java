package com.example.javaembarque;

import android.Manifest;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    String url;
    ProgressBar mProgressBar;
    EditText texte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        texte = (EditText) findViewById(R.id.uri);
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        btn_download = (Button) findViewById(R.id.download_btn);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_download.setEnabled(false);
                url = texte.getText().toString();
                mProgressBar.setProgress(0);
                mProgressBar.setMax(100);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                File file = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "My_Video", "video.mp4");
                request.setDestinationUri(Uri.fromFile(file));
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        boolean downloading = true;
                        while (downloading) {
                            DownloadManager.Query q = new DownloadManager.Query();
                            Cursor cursor = manager.query(q);
                            cursor.moveToFirst();
                            int bytes_downloaded = cursor.getInt(cursor
                                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                downloading = false;
                            }

                            final int dl_progress = (int) ((double)bytes_downloaded / (double)bytes_total * 100f);

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mProgressBar.setProgress((int) dl_progress);
                                    if(dl_progress == 100){
                                        btn_download.setEnabled(true);
                                    }
                                }
                            });

                            cursor.close();
                        }

                    }
                }).start();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Long reference = manager.enqueue(request);

            }
        });
    }
}