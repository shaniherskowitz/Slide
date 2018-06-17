package com.example.shaniherskowitz.slide;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class ImageServiceService extends Service {

    public ImageServiceService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Creates the service
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Here put the Code of Service
    }

    /**
     * Show a toast that service is running
     * @param intent - activity
     * @param flag - int flat
     * @param startId - get the id
     * @return
     */
    public int onStartCommand(Intent intent, int flag, int startId) {
        Toast.makeText(this,"Service starting...", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    /**
     * Toast to show the service is ended - no longer running
     */
    public void onDestroy() {
        Toast.makeText(this,"Service ending...", Toast.LENGTH_SHORT).show();
    }

}
