package com.example.javaembarque;

import android.Manifest;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ServerActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btn_download;
    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    BluetoothServerSocket serverSocket;
    BluetoothSocket socket;
    String url;
    //ProgressBar mProgressBar;
    EditText texte;
    TextView text;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        texte = (EditText) findViewById(R.id.uri);
        text = (TextView) findViewById(R.id.serv);
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        btn_download = (Button) findViewById(R.id.download_btn);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        Button btn_serv = (Button) findViewById(R.id.start_btn);
        btn_serv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bluetooth = BluetoothAdapter.getDefaultAdapter();
                        if (bluetooth == null) {
                            text.setText("Does not support bluetooth");
                            return;
                        }

                        Intent discoverableIntent = new
                                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);
                        text.setText("Discoverable!!");

                        AcceptThread thread = new AcceptThread();
                        thread.start();

                        //while(serverSocket==null);
                        //AcceptThread();

                        /*new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BluetoothSocket socket = null;
                                System.out.println("efhheqfuhiuhfuiazuigffggfoefopooqjeriogiogvhruUIFGVzgiojfkefokpokfezpokokpodvk");
                                while (true) {
                                    try {
                                        socket = serverSocket.accept();
                                        text.setText("listening");
                                    } catch (IOException e) {
                                        break;
                                    }
                                    if (socket != null) {
                                        text.setText("done");
                                        try {
                                            serverSocket.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
                                }
                            }
                        });*/
                    }
        });

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_download.setEnabled(false);
                url = texte.getText().toString();
                //mProgressBar.setProgress(0);
                //mProgressBar.setMax(100);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                File file = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "My_Video", "video.mp4");
                request.setDestinationUri(Uri.fromFile(file));
                /*new Thread(new Runnable() {

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
                }).start();*/
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Long reference = manager.enqueue(request);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(socket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Handler handler=new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1: text.setText("Connected");

                    break;
                case 2: text.setText("Connection failed");
                    break;
                case 5:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    sendReceive.write(socket);
                    break;
            }

            return true;
        }
    });

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetooth.listenUsingRfcommWithServiceRecord("MYYAPP", UUID.fromString("c89fe610-2273-4d4f-bb49-08430113de66"));
            } catch (IOException e) {
                Log.e("TAG", "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    Message message = Message.obtain();
                    message.what = 1;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                } catch (IOException e) {
                    Log.e("TAG", "Socket's accept() method failed", e);
                    Message message = Message.obtain();
                    message.what = 2;
                    handler.sendMessage(message);
                    break;
                }

            }
        }

    }

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private OutputStream outputStream;
        public SendReceive(BluetoothSocket socket){
            bluetoothSocket=socket;
            InputStream tempIn =null;
            OutputStream tempOut =null;


            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(5,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        public void write(BluetoothSocket socket){
            try {
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) { }
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + File.separator + "My_Video/video.mp4");
            int size = (int) file.length();
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(size);
            write(bb.array());
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.write(bytes);
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                outputStream.write("EOF".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}