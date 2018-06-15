package com.example.shaniherskowitz.slide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainSlide extends AppCompatActivity {

    private BroadcastReceiver receiver;
    private Thread client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_slide);
        System.out.println("yayyy");


    }


    public void serviceSwitch(View view) {
        Switch s = (Switch)findViewById(R.id.switch1);
        if(s.isChecked()) {
            System.out.println("yayyy");
            Intent intent = new Intent(this, ImageServiceService.class);
            startService(intent);
            try {
                client = new ClientConnection();
                client.start();
            } catch (Exception e) {}
            broadcast();

        } else {
            System.out.println("nayyy");
            Intent intent = new Intent(this, ImageServiceService.class);
            stopService(intent);
        }
    }

    public void getPics() {
        // Getting the Camera Folder
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if(dcim != null) return;
        try {
            File[] pics = dcim.listFiles();
            int count = 0;
            if (pics != null) {
                for (File pic : pics) {
                    byte[] byte_pic = picToByte(pic);
                    connect(byte_pic, pic);

                }
            }
        } catch(Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    public void progBar() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setContentTitle("Picture Transfer");
        builder.setContentText("Transfer in progress");
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setContentText("Half way through");
        builder.setProgress(100, 50, false);
        notificationManager.notify(1, builder.build());
        builder.setContentText("Download complete");
        builder.setProgress(0, 0, false);
        notificationManager.notify(1, builder.build());

    }

    public void connect(byte[] imgbyte, File pic) {
        try {
            InetAddress serverAddr = InetAddress.getByName("10.0.2.2");

            Socket socket = new Socket(serverAddr, 6789);

            try {

                OutputStream output = socket.getOutputStream();
                FileInputStream fis = new FileInputStream(pic);
                output.write(imgbyte);
                output.flush();


            } catch (Exception e) {
                Log.e("TCP", "S:Error", e);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    public byte[] picToByte(File pic) {
        try {
            FileInputStream fis = new FileInputStream(pic);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            return getBytesFromBitmap(bm);
        } catch(Exception e) {
            Log.e("TCP", "C: Error", e);
        }
        return null;
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    public void broadcast() {
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(wifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) { //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            startTransfer();// Starting the Transfer
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.receiver, theFilter);
    }

    public void startTransfer() {
        getPics();
    }
}
