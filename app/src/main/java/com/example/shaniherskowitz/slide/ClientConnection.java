package com.example.shaniherskowitz.slide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection extends Thread {

    public Socket socket;
    private Scanner scanner;

    public ClientConnection() {


    }

    public void run() {
        try {
            this.socket = new Socket("10.0.2.2", 9000);
            this.scanner = new Scanner(System.in);
            System.out.println("\r\nConnected to Server: " + "10.0.2.2");
            startTransfer();
        } catch(Exception e) {

        }

    }

    public void start1() throws IOException {
        String input;
        while (true) {
            input = scanner.nextLine();
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(input);
            out.flush();
        }
    }


    public void startTransfer() {
        getPics();
    }


    public void connect(byte[] imgbyte, File pic) {
        try {
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
}
