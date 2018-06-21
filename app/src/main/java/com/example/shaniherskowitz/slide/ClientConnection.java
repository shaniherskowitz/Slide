package com.example.shaniherskowitz.slide;

import android.app.AppComponentFactory;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientConnection extends Thread {

    public Socket socket;
    private Scanner scanner;
    DataOutputStream dOut;
    private NotificationCompat.Builder builder;
    private NotificationManager nm;
    private BroadcastReceiver receiver;
    private AppCompatActivity main;


    private boolean isConnected;
    private boolean on;
    private boolean first;

    /**
     * Creates the client connection
     */
    public ClientConnection(NotificationCompat.Builder builder, NotificationManager nm,
                            AppCompatActivity main, boolean on) {
        this.builder = builder;
        this.nm = nm;
        this.builder.setSmallIcon(R.drawable.image);
        this.main = main;
        this.on = on;
        this.first = true;
        this.isConnected = true;

    }

    public void setOn(boolean on) {
        this.on = on;
    }


    /**
     * Runs the client
     */
    public void run() {
        try {
            //creates a socket for the client
            //connectToServer();
            //transfer images to the server
            broadcast();

        } catch (Exception e) {
        }
    }

    public void connectToServer() {

        try {
            //creates a socket for the client
            this.socket = new Socket("10.0.2.2", 9000);
            this.scanner = new Scanner(System.in);
            dOut = new DataOutputStream(socket.getOutputStream());
            System.out.println("\r\nConnected to Server: " + "10.0.2.2");

        } catch (Exception e) {
        }
    }

    /**
     * @throws IOException
     */
    public void start1() throws IOException {
        String input;
        while (true) {
            input = scanner.nextLine();
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(input);
            out.flush();
        }
    }

    public boolean isConnected() {
        return isConnected;
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

                            if (on && first) {
                                startTransfer();
                                first = false;
                                isConnected = true;
                            }
                        } else {
                            first = true;
                            isConnected = false;
                        }

                    }
                }
            }
        };
        main.registerReceiver(this.receiver, theFilter);


    }


    /**
     * Gets the pictures to transfer to the server
     */
    public void startTransfer() {
        builder.setContentText("Transfer in progress");
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setProgress(100, 0, false);
        nm.notify(1, builder.build());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getPics();
            }
        });
        thread.start();


    }


    /**
     * Send images to the server
     *
     * @param imgbyte - the image want to send
     * @param pic     - file want to send
     */
    public void connect(byte[] imgbyte, File pic) {
        try {
            try {

                dOut.writeInt(imgbyte.length); // write length of the message
                dOut.write(imgbyte);
                //output.write(imgbyte);
                //output.flush();

            } catch (Exception e) {
                Log.e("TCP", "S:Error", e);
            } finally {
                //socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }


    /**
     * Convert the image to bytes
     *
     * @param pic - the picture want to convet
     * @return - image converted to bytes
     */
    public byte[] picToByte(File pic) {
        try {
            FileInputStream fis = new FileInputStream(pic);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            return getBytesFromBitmap(bm);
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
        return null;
    }

    /**
     * Gets teh bytes from bitmap
     *
     * @param bitmap - the bitmap
     * @return an array of bytes
     */
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    /**
     * Gets the pictures from the dcim file
     */
    public void getPics() {
        // Getting the Camera Folder
        connectToServer();
        File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (dcim == null) return;
        try {
            File[] pics = dcim.listFiles();
            int count = pics.length;
            dOut.writeInt(pics.length); // write length of the message
            if (pics != null) {
                File pictures = pics[0];
                for (File pic : pics) {
                    byte[] byte_pic = picToByte(pic);
                    //calls connect with the picture in bytes and the file
                    connect(byte_pic, pic);
                    if (count == (pics.length / 2)) {
                        builder.setContentText("Half way through");
                        builder.setProgress(100, 50, false);
                        nm.notify(1, builder.build());
                    }
                    count--;
                }
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        } finally {
            try {
                socket.close();
                try {
                    nm.notify(1, builder.build());
                    builder.setContentText("Download complete");
                    builder.setProgress(100, 100, false);
                    nm.notify(1, builder.build());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } catch (Exception d) {

            }

        }
    }
}
