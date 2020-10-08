package com.example.javaembarque;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Bluetooth extends AppCompatActivity {

    ListView scrollView;
    BluetoothAdapter adapter;
    Button refresh, download;
    TextView text;
    SendReceive sendReceive;
    private FileOutputStream fos;
    File file;

    private ArrayList<String> deviceNames;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        text = findViewById(R.id.test);
        scrollView = findViewById(R.id.bluetooth);
        adapter = BluetoothAdapter.getDefaultAdapter();
        refresh = findViewById(R.id.refreshButton);
        download = findViewById(R.id.download_btn);
        download.setEnabled(false);

        deviceNames = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, deviceNames);
        scrollView.setAdapter(arrayAdapter);

        String enableRequest = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        startActivityForResult(new Intent(enableRequest), 0);

        File file = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "My_Video", "video.mp4");
        try {
            fos = new FileOutputStream(file,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!adapter.isEnabled()) {
            String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
            String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
            registerReceiver(adapterState, new IntentFilter(actionStateChanged));
            if(checkSelfPermission(Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN},0);
            }
            startActivityForResult(new Intent(actionRequestEnable), 0);
        }

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt= "test com";
                sendReceive.write(txt.getBytes());
                download.setEnabled(false);
            }
        });


        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.isDiscovering()) {
                    adapter.cancelDiscovery();
                }
                ActivityCompat.requestPermissions(Bluetooth.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
                if (adapter!=null){
                    boolean result = adapter.startDiscovery();
                    Log.i("Info", "adapter state int="+adapter.getState());
                    Log.i("Info", "Discovering="+result);
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryMonitor,filter);
        registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        scrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                try {
                    BluetoothSocket socket = devices.get(position).createInsecureRfcommSocketToServiceRecord(UUID.fromString("c89fe610-2273-4d4f-bb49-08430113de66"));
                    socket.connect();
                    Intent intent = new Intent(getApplicationContext(),VideoPlayer.class);
                    if(socket.isConnected()) {
                        refresh.setText("Connecté");
                        refresh.setEnabled(false);
                        download.setEnabled(true);
                        sendReceive = new SendReceive(socket);
                        sendReceive.start();
                    }
                    else{
                        refresh.setText("Failed");
                    }
                    //startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if(adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        ActivityCompat.requestPermissions(Bluetooth.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
        if (adapter!=null){
            boolean result = adapter.startDiscovery();
            Log.i("Info", "adapter state int="+adapter.getState());
            Log.i("Info", "Discovering="+result);
        }
    }

    BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
        String dStarted = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
        String dFinished = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (dStarted.equals(intent.getAction())) {
                Toast.makeText(getApplicationContext(), "Discovery Started. . .", Toast.LENGTH_SHORT).show();
                deviceNames.clear();
                arrayAdapter.notifyDataSetChanged();
            } else if (dFinished.equals(intent.getAction())) {
                Toast.makeText(getApplicationContext(), "Discovery Completed. . .", Toast.LENGTH_SHORT).show();
            }
        }
    };

    BroadcastReceiver adapterState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            String tt = "";
            switch (state) {
                case (BluetoothAdapter.STATE_TURNING_ON): {
                    tt = "Bluetooth turning on";
                    break;
                }
                case (BluetoothAdapter.STATE_ON): {
                    tt = "Bluetooth on";
                    unregisterReceiver(this);
                    break;
                }
                case (BluetoothAdapter.STATE_TURNING_OFF): {
                    tt = "Bluetooth turning off";
                    break;
                }
                case (BluetoothAdapter.STATE_OFF): {
                    tt = "Bluetooth off";
                    break;
                }
                default:
                    break;
            }
            Toast.makeText(getApplicationContext(), tt, Toast.LENGTH_SHORT).show();

        }
    };

    BroadcastReceiver discoveryResult = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            if(deviceName != null) {
                if(!deviceNames.contains(deviceName)) {
                    devices.add(device);
                    deviceNames.add(deviceName);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    Handler handler=new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1: text.setText("Connected");
                    break;
                case 2: text.setText("Connection failed");
                case 5:
                    byte[] text = (byte[]) msg.obj;
                    String str = new String(text);
                    if(!str.equals("EOF")){
                        try {
                            fos.write(text);
                            System.out.println(str);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        download.setEnabled(true);
                    }
                    break;
            }

            return true;
        }
    });

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
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
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}