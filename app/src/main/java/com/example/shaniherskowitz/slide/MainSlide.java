package com.example.shaniherskowitz.slide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class MainSlide extends AppCompatActivity {

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
