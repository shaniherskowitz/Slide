package com.example.shaniherskowitz.slide;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class MainSlide extends AppCompatActivity {

    private final int  MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private Context context;
    private BroadcastReceiver receiver;
    private Thread client;

    /**
     * Create the app
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_slide);
        context = this;
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {

        }
    }

        @Override
        public void onRequestPermissionsResult ( int requestCode,
        String permissions[], int[] grantResults){
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                    }
                }
            }
        }


    /**
     * Deals with the service switch - when it is on and off
     * @param view - the app view
     */
    public void serviceSwitch(View view) {
        Switch s = findViewById(R.id.switch1);
        if(s.isChecked()) {
            //create an intent as an activity to either start or stop the service
            Intent intent = new Intent(this, ImageServiceService.class);
            startService(intent);
            //send images when connected to wifi
            broadcast();
            progBar();
            try {
                //create the connection with the client
                client = new ClientConnection();

            } catch (Exception e) {}

        } else {
            Intent intent = new Intent(this, ImageServiceService.class);
            stopService(intent);
        }
    }

    /**
     * For the progress bar
     */
    public void progBar() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle("Picture Transfer");
        builder.setContentText("Transfer in progress");
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setContentText("Half way through");
        
        builder.setProgress(100, 50, false);
        builder.setSmallIcon(R.drawable.image);
        try {
            nm.notify(1, builder.build());
            builder.setContentText("Download complete");
            builder.setProgress(0, 0, false);
            nm.notify(1, builder.build());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    /**
     * only send images when connected to wifi
     */
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
                            client.start();
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.receiver, theFilter);
    }
}
